package com.sportos.watch.core.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Compact live beacon telemetry packet.
 * Replaces verbose JSON with a pipe-delimited lightweight format,
 * reducing transmitted bytes by >75% to minimize LTE/Wi-Fi radio transmit windows.
 */
data class CompactBeaconPacket(
    val sport: String,
    val elapsedSec: Long,
    val heartRate: Int,
    val calories: Int,
    val distanceMeters: Int,
    val speedKmh: Float,
    val primaryStat: String,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    /**
     * Serializes into compact format:
     * "sport|elapsedSec|hr|cal|distM|speedKmh|lat|lng|stat"
     */
    fun toCompactString(): String {
        val latStr = if (latitude != null) String.format(Locale.US, "%.5f", latitude) else ""
        val lngStr = if (longitude != null) String.format(Locale.US, "%.5f", longitude) else ""
        val speedStr = String.format(Locale.US, "%.1f", speedKmh)
        return "$sport|$elapsedSec|$heartRate|$calories|$distanceMeters|$speedStr|$latStr|$lngStr|$primaryStat"
    }

    companion object {
        fun fromCompactString(raw: String): CompactBeaconPacket? {
            val parts = raw.split("|")
            if (parts.size < 9) return null
            return CompactBeaconPacket(
                sport = parts[0],
                elapsedSec = parts[1].toLongOrNull() ?: 0L,
                heartRate = parts[2].toIntOrNull() ?: 0,
                calories = parts[3].toIntOrNull() ?: 0,
                distanceMeters = parts[4].toIntOrNull() ?: 0,
                speedKmh = parts[5].toFloatOrNull() ?: 0f,
                latitude = parts[6].toDoubleOrNull(),
                longitude = parts[7].toDoubleOrNull(),
                primaryStat = parts[8]
            )
        }
    }
}

/**
 * Battery-optimized Ktor WebSocket client for live spectator beaconing.
 *
 * Optimizations:
 * 1. Adaptive Duty-Cycling: Transmits every 15s (active) or 30s (paused) instead of 5s,
 *    allowing Wear OS LTE/Wi-Fi modems to enter low-power DRX sleep states.
 * 2. Network Intelligence: Verifies connectivity before attempting socket handshakes.
 * 3. Exponential Backoff: Protects battery from aggressive socket reconnect loops.
 * 4. Compact Payload: Slashes packet overhead from ~200B to ~40B.
 */
class LiveBeaconClient(
    private val networkMonitor: NetworkMonitor? = null
) {
    private val client by lazy {
        HttpClient(CIO) {
            install(WebSockets) {
                pingInterval = 30_000
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 5_000
                requestTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }
        }
    }

    private var beaconJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun startStreaming(
        token: String,
        beaconId: String,
        isPausedProvider: () -> Boolean = { false },
        payloadProvider: () -> CompactBeaconPacket
    ) {
        beaconJob?.cancel()
        beaconJob = scope.launch {
            var backoffDelayMs = 5_000L

            while (isActive) {
                // 1. Passive network check: avoid waking up modem if device has no internet
                if (networkMonitor != null && !networkMonitor.isOnline()) {
                    Log.d(TAG, "Device offline; deferring beacon transmission")
                    delay(15_000L)
                    continue
                }

                try {
                    val url = "ws://localhost:8080/beacon/$beaconId?token=$token"
                    client.webSocket(urlString = url) {
                        Log.d(TAG, "Live Beacon Connected")
                        backoffDelayMs = 5_000L // Reset backoff on successful connection

                        while (isActive) {
                            val packet = payloadProvider()
                            send(Frame.Text(packet.toCompactString()))

                            // Adaptive duty cycle: 15s active, 30s paused to maximize radio sleep
                            val sleepDuration = if (isPausedProvider()) 30_000L else 15_000L
                            delay(sleepDuration)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Beacon connection interrupted: ${e.message}. Retrying in ${backoffDelayMs}ms")
                    delay(backoffDelayMs)
                    backoffDelayMs = (backoffDelayMs * 2).coerceAtMost(60_000L)
                }
            }
        }
    }

    fun stopStreaming() {
        beaconJob?.cancel()
        beaconJob = null
    }

    fun close() {
        stopStreaming()
        try {
            client.close()
        } catch (_: Exception) {}
    }

    companion object {
        private const val TAG = "LiveBeaconClient"
    }
}
