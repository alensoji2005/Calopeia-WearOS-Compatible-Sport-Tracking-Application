package com.sportos.watch.core.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
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

class LiveBeaconClient {

    private val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 20_000
        }
    }

    private var beaconJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Connects to the SportOS live WebSocket server over Wi-Fi/LTE 
     * and streams a JSON payload every 5 seconds.
     */
    fun startStreaming(token: String, beaconId: String, payloadProvider: () -> String) {
        beaconJob?.cancel()
        beaconJob = scope.launch {
            try {
                // Example URL, in a real app would be wss://api.sportos.com/live
                client.webSocket(urlString = "ws://localhost:8080/beacon/$beaconId?token=$token") {
                    Log.d(TAG, "Live Beacon Connected")
                    
                    while (isActive) {
                        val payload = payloadProvider()
                        send(Frame.Text(payload))
                        delay(5000L) // Broadcast every 5s to preserve battery
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Live Beacon Connection Error", e)
            }
        }
    }

    fun stopStreaming() {
        beaconJob?.cancel()
        beaconJob = null
    }

    fun close() {
        client.close()
    }

    companion object {
        private const val TAG = "LiveBeaconClient"
    }
}
