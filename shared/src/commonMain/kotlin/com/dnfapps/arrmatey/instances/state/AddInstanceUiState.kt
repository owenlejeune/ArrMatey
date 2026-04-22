package com.dnfapps.arrmatey.instances.state

import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType

data class AddInstanceUiState(
    val apiEndpoint: String = "",
    val apiKey: String = "",
    val basicAuthEnabled: Boolean = false,
    val instanceLabel: String = "",
    val isSlowInstance: Boolean = false,
    val customTimeout: Long? = null,
    val endpointError: Boolean = false,
    val testing: Boolean = false,
    val localTesting: Boolean = false,
    val testResult: Boolean? = null,
    val localTestResult: Boolean? = null,
    val saveButtonEnabled: Boolean = false,
    val createResult: InsertResult? = null,
    val editResult: InsertResult? = null,
    val infoCardMaps: Map<InstanceType, Boolean> = emptyMap(),
    val headers: List<InstanceHeader> = emptyList(),
    val localNetworkEnabled: Boolean = false,
    val localNetworkUrl: String = "",
    val localNetworkSsids: List<String> = emptyList(),
    val localNetworkUrlError: Boolean = false,
    val notificationsEnabled: Boolean = false
) {
    constructor(): this("") // helper for iOS

    val localNetworkConfigured: Boolean
        get() = localNetworkEnabled && localNetworkUrl.isNotEmpty() && localNetworkSsids.isNotEmpty()
}