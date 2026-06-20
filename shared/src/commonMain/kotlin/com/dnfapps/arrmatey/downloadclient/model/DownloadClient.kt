package com.dnfapps.arrmatey.downloadclient.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.utils.getNetworkUtils

@Entity(
    tableName = "download_clients",
    indices = [
        Index(value = ["url"], unique = true),
        Index(value = ["label"], unique = true)
    ]
)
data class DownloadClient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: DownloadClientType,
    val label: String,
    val url: String,
    val username: EncryptedString = EncryptedString(""),
    val password: EncryptedString = EncryptedString(""),
    val apiKey: EncryptedString = EncryptedString(""),
    val noApiKeyRequired: Boolean = false,
    val selected: Boolean = false,
    val headers: List<InstanceHeader> = emptyList(),

    val localNetworkEnabled: Boolean = false,
    val localNetworkSsids: List<String> = emptyList(),
    val localNetworkEndpoint: String? = null
) {

    fun getEffectiveBaseUrl(): String {
        if (!localNetworkEnabled ||
            localNetworkSsids.isEmpty() ||
            localNetworkEndpoint.isNullOrBlank()
        ) {
            return url
        }
        return try {
            val currentSsid = getNetworkUtils().getCurrentWifiSsid()
            if (currentSsid != null && localNetworkSsids.any { it.equals(currentSsid, ignoreCase = true) }) {
                localNetworkEndpoint
            } else {
                url
            }
        } catch (e: Exception) {
            url
        }
    }

    fun isUsingLocalNetwork(): Boolean {
        return try {
            val currentSsid = getNetworkUtils().getCurrentWifiSsid()
            localNetworkEnabled &&
                    !localNetworkEndpoint.isNullOrBlank() &&
                    currentSsid != null &&
                    localNetworkSsids.any { it.equals(currentSsid, ignoreCase = true) }
        } catch (e: Exception) {
            false
        }
    }
}
