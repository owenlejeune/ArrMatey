package com.dnfapps.arrmatey.backup.model

import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.Serializable

@Serializable
data class InstanceExport(
    val type: InstanceType,
    val label: String,
    val url: String,
    val apiKey: String,
    val noApiKeyRequired: Boolean = false,
    val enabled: Boolean = true,
    val slowInstance: Boolean = false,
    val customTimeout: Long? = null,
    val notificationsEnabled: Boolean = true,
    val headers: List<InstanceHeader> = emptyList(),
    val localNetworkEnabled: Boolean = false,
    val localNetworkSsids: List<String> = emptyList(),
    val localNetworkEndpoint: String? = null,
    val preferences: InstancePreferences? = null,
)
