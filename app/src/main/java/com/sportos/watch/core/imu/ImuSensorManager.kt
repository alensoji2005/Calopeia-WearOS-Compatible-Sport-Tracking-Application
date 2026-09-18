package com.sportos.watch.core.imu

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class ImuData(
    val timestamp: Long,
    val accX: Float, val accY: Float, val accZ: Float,
    val gyroX: Float, val gyroY: Float, val gyroZ: Float
)

class ImuSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _imuFlow = MutableSharedFlow<ImuData>(extraBufferCapacity = 100)
    val imuFlow: SharedFlow<ImuData> = _imuFlow.asSharedFlow()

    // Pre-allocated primitive buffers to eliminate garbage collection in 50Hz hot-path
    private val accBuffer = FloatArray(3)
    private val gyroBuffer = FloatArray(3)
    private var hasAcc = false
    private var hasGyro = false

    private var isAccRegistered = false
    private var isGyroRegistered = false

    /**
     * Starts listening with hardware FIFO batching and selective sensor gating.
     * @param includeAccelerometer whether to activate 3-axis accelerometer
     * @param includeGyroscope whether to activate 3-axis gyroscope (power-heavy)
     * @param maxReportLatencyUs FIFO hardware buffer delay (default 150ms to allow CPU deep sleep)
     */
    fun startListening(
        includeAccelerometer: Boolean = true,
        includeGyroscope: Boolean = true,
        maxReportLatencyUs: Int = 150_000 // 150ms batching saves ~70% CPU interrupts
    ) {
        stopListening()

        isAccRegistered = includeAccelerometer && (accelerometer != null)
        isGyroRegistered = includeGyroscope && (gyroscope != null)

        if (isAccRegistered) {
            accelerometer?.let {
                sensorManager.registerListener(
                    this, 
                    it, 
                    SensorManager.SENSOR_DELAY_GAME, 
                    maxReportLatencyUs
                )
            }
        }

        if (isGyroRegistered) {
            gyroscope?.let {
                sensorManager.registerListener(
                    this, 
                    it, 
                    SensorManager.SENSOR_DELAY_GAME, 
                    maxReportLatencyUs
                )
            }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        isAccRegistered = false
        isGyroRegistered = false
        hasAcc = false
        hasGyro = false
        accBuffer.fill(0f)
        gyroBuffer.fill(0f)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accBuffer, 0, 3)
                hasAcc = true
                emitImuIfReady(event.timestamp)
            }
            Sensor.TYPE_GYROSCOPE -> {
                System.arraycopy(event.values, 0, gyroBuffer, 0, 3)
                hasGyro = true
                emitImuIfReady(event.timestamp)
            }
        }
    }

    private fun emitImuIfReady(timestamp: Long) {
        // Robust gating: If a sensor is not registered or not present on hardware, don't block
        val ready = when {
            isAccRegistered && isGyroRegistered -> hasAcc && hasGyro
            isAccRegistered -> hasAcc
            isGyroRegistered -> hasGyro
            else -> false
        }

        if (ready) {
            val imuData = ImuData(
                timestamp = timestamp,
                accX = if (isAccRegistered) accBuffer[0] else 0f,
                accY = if (isAccRegistered) accBuffer[1] else 0f,
                accZ = if (isAccRegistered) accBuffer[2] else 0f,
                gyroX = if (isGyroRegistered) gyroBuffer[0] else 0f,
                gyroY = if (isGyroRegistered) gyroBuffer[1] else 0f,
                gyroZ = if (isGyroRegistered) gyroBuffer[2] else 0f
            )
            _imuFlow.tryEmit(imuData)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}

/**
 * A basic Lock-Free Circular Ring Buffer for maintaining a window of IMU data 
 * to detect peaks (e.g. for shot detection).
 */
class ImuRingBuffer(private val capacity: Int) {
    private val buffer = Array<ImuData?>(capacity) { null }
    private var head = 0
    private var size = 0

    fun add(data: ImuData) {
        buffer[head] = data
        head = (head + 1) % capacity
        if (size < capacity) {
            size++
        }
    }

    fun getValues(): List<ImuData> {
        val list = mutableListOf<ImuData>()
        var i = if (size == capacity) head else 0
        for (count in 0 until size) {
            buffer[i]?.let { list.add(it) }
            i = (i + 1) % capacity
        }
        return list
    }
    
    fun clear() {
        head = 0
        size = 0
    }
}
