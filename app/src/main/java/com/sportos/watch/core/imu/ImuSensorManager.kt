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

    // Temporary storage to merge acc and gyro data based on closest timestamp
    private var lastAcc: FloatArray? = null
    private var lastGyro: FloatArray? = null

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) // ~50Hz
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
        lastAcc = null
        lastGyro = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                lastAcc = event.values.clone()
                emitImuIfReady(event.timestamp)
            }
            Sensor.TYPE_GYROSCOPE -> {
                lastGyro = event.values.clone()
                emitImuIfReady(event.timestamp)
            }
        }
    }

    private fun emitImuIfReady(timestamp: Long) {
        val acc = lastAcc
        val gyro = lastGyro
        if (acc != null && gyro != null) {
            val imuData = ImuData(
                timestamp = timestamp,
                accX = acc[0], accY = acc[1], accZ = acc[2],
                gyroX = gyro[0], gyroY = gyro[1], gyroZ = gyro[2]
            )
            _imuFlow.tryEmit(imuData)
            // Clear values after emit if we want strict pair-wise, 
            // but for typical IMU fusion we hold the last known value 
            // of the slower sensor. Let's keep them so the faster sensor 
            // can pair with the most recent sample of the other.
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op for now
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
