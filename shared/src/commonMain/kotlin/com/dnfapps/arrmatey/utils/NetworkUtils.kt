package com.dnfapps.arrmatey.utils

interface NetworkUtils {
    fun getCurrentWifiSsid(): String?

    fun isConnectedToWifi(): Boolean

    fun isConnectedToSsid(ssid: String): Boolean {
        val currentSsid = getCurrentWifiSsid()
        return currentSsid != null && currentSsid.equals(ssid, ignoreCase = true)
    }
}

expect fun getNetworkUtils(): NetworkUtils
