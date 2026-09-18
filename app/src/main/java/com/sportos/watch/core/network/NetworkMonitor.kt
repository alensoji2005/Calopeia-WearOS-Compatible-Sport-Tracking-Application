package com.sportos.watch.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Lightweight Wear OS network connectivity observer.
 * Enables radio duty-cycling and prevents high-power modem wakeups when offline.
 */
class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    /**
     * Checks if the watch has active, validated internet connectivity.
     */
    fun isOnline(): Boolean {
        val cm = connectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Checks if the active connection is a low-power transport:
     * - Bluetooth proxy via paired smartphone (TRANSPORT_BLUETOOTH)
     * - Wi-Fi (TRANSPORT_WIFI)
     * versus high-power cellular modem (TRANSPORT_CELLULAR).
     */
    fun isLowPowerTransport(): Boolean {
        val cm = connectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) ||
               caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Returns true if the device is specifically transmitting over high-power cellular LTE.
     */
    fun isCellular(): Boolean {
        val cm = connectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
