package com.dnfapps.arrmatey.utils

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresPermission

class AndroidNetworkUtils(
    private val context: Context,
) : NetworkUtils {
    private val connectivityManager: ConnectivityManager? by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }

    private val wifiManager: WifiManager? by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun getCurrentWifiSsid(): String? {
        if (!isConnectedToWifi()) return null

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 and above
            getWifiSsidModern()
        } else {
            // Below Android 10
            getWifiSsidLegacy()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun isConnectedToWifi(): Boolean {
        val cm = connectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun getWifiSsidModern(): String? {
        val wm = wifiManager ?: return null
        val wifiInfo = wm.connectionInfo ?: return null
        return cleanSsid(wifiInfo.ssid)
    }

    @Suppress("DEPRECATION")
    private fun getWifiSsidLegacy(): String? {
        val wm = wifiManager ?: return null
        val wifiInfo = wm.connectionInfo ?: return null
        return cleanSsid(wifiInfo.ssid)
    }

    /**
     * Clean SSID by removing quotes and <unknown ssid>
     */
    private fun cleanSsid(ssid: String?): String? {
        if (ssid.isNullOrBlank()) return null

        // Remove common invalid values
        if (ssid in listOf("<unknown ssid>", "0x", "null")) {
            return null
        }

        // Remove quotes that Android adds
        var cleaned = ssid.trim()
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length - 1)
        }

        return cleaned.takeIf { it.isNotBlank() }
    }
}

// Global instance holder
private var networkUtilsInstance: NetworkUtils? = null

/**
 * Initialize NetworkUtils with Android Context
 * Call this from your Application class or MainActivity
 */
fun initializeNetworkUtils(context: Context) {
    networkUtilsInstance = AndroidNetworkUtils(context.applicationContext)
}

actual fun getNetworkUtils(): NetworkUtils =
    networkUtilsInstance
        ?: throw IllegalStateException("NetworkUtils not initialized. Call initializeNetworkUtils(context) first.")
