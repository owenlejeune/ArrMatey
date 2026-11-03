package com.dnfapps.arrmatey.backup.model

import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import kotlinx.serialization.Serializable

@Serializable
data class MediaServerExport(
    val serverType: MediaServerType,
    val label: String,
    val url: String,
    val token: String,
    val userId: String? = null,
    val username: String? = null,
    val clientIdentifier: String,
    val serverId: String? = null,
    val enabled: Boolean = true,
    val localNetworkEnabled: Boolean = false,
    val localNetworkSsids: List<String> = emptyList(),
    val localNetworkEndpoint: String? = null,
    val headers: List<InstanceHeader> = emptyList(),
)
