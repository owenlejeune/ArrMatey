package com.dnfapps.arrmatey.downloadclient.state

import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import com.dnfapps.arrmatey.instances.model.InstanceHeader

data class DownloadClientConfigurationUiState(
    val label: String = "",
    val selectedType: DownloadClientType = DownloadClientType.QBittorrent,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val apiKey: String = "",
    val enabled: Boolean = true,
    val headers: List<InstanceHeader> = emptyList(),
    val isEditing: Boolean = false,
    val saveButtonEnabled: Boolean = false,
    val isTesting: Boolean = false,
    val endpointError: Boolean = false,
    val connectionState: OperationStatus = OperationStatus.Idle,
    val mutationState: DownloadClientMutationState = DownloadClientMutationState.Initial
) {
    constructor(): this(label = "") // ios overload
}