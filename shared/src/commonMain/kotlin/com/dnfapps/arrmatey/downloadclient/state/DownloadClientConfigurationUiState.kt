package com.dnfapps.arrmatey.downloadclient.state

import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.model.OperationStatus

data class DownloadClientConfigurationUiState(
    val label: String = "",
    val selectedType: DownloadClientType = DownloadClientType.QBittorrent,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val apiKey: String = "",
    val noApiKeyRequired: Boolean = false,
    val headers: List<InstanceHeader> = emptyList(),
    val isEditing: Boolean = false,
    val saveButtonEnabled: Boolean = false,
    val isTesting: Boolean = false,
    val endpointError: Boolean = false,
    val connectionState: OperationStatus = OperationStatus.Idle,
    val mutationState: DownloadClientMutationState = DownloadClientMutationState.Initial,
    val localNetworkEnabled: Boolean = false,
    val localNetworkSsids: List<String> = emptyList(),
    val localNetworkEndpoint: String = "",
    val localNetworkEndpointError: Boolean = false,
    val localTesting: Boolean = false,
    val localTestResult: Boolean? = null,
    val testResult: Boolean? = null,
) {
    val localNetworkConfigured: Boolean
        get() = localNetworkEnabled && localNetworkSsids.isNotEmpty() && localNetworkEndpoint.isNotBlank()

    constructor() : this(label = "") // ios overload
}
