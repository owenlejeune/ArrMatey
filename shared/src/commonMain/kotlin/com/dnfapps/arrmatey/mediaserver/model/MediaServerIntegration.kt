package com.dnfapps.arrmatey.mediaserver.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.utils.getNetworkUtils

@Entity(
    tableName = "media_server_integrations",
    indices = [
        Index(value = ["serverType"], unique = true),
    ],
)
data class MediaServerIntegration(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverType: MediaServerType,
    val label: String,
    val url: String,
    val token: EncryptedString,
    val userId: String? = null,
    val username: String? = null,
    val clientIdentifier: String,
    val serverId: String? = null,
    val enabled: Boolean = true,
    val localNetworkEnabled: Boolean = false,
    val localNetworkSsids: List<String> = emptyList(),
    val localNetworkEndpoint: String? = null,
    val headers: List<InstanceHeader> = emptyList(),
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

    fun isUsingLocalNetwork(): Boolean = try {
        val currentSsid = getNetworkUtils().getCurrentWifiSsid()
        localNetworkEnabled &&
            !localNetworkEndpoint.isNullOrBlank() &&
            currentSsid != null &&
            localNetworkSsids.any { it.equals(currentSsid, ignoreCase = true) }
    } catch (e: Exception) {
        false
    }
}
