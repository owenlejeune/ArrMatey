package com.dnfapps.arrmatey.backup.model

import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import kotlinx.serialization.Serializable

@Serializable
data class DownloadClientExport(
    val type: DownloadClientType,
    val label: String,
    val url: String,
    val username: String = "",
    val password: String = "",
    val apiKey: String = "",
    val noApiKeyRequired: Boolean = false,
    val headers: List<InstanceHeader> = emptyList(),
    val localNetworkEnabled: Boolean = false,
    val localNetworkSsids: List<String> = emptyList(),
    val localNetworkEndpoint: String? = null,
)
