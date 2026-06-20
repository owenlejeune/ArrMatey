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
    val noApiKeyRequired: Boolean,
    val enabled: Boolean,
    val slowInstance: Boolean,
    val customTimeout: Long?,
    val notificationsEnabled: Boolean,
    val headers: List<InstanceHeader>,
    val localNetworkEnabled: Boolean,
    val localNetworkSsids: List<String>,
    val localNetworkEndpoint: String?,
    val preferences: InstancePreferences? = null
)
