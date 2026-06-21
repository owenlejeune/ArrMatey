package com.dnfapps.arrmatey.backup.state

import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.instances.model.Instance

data class ExportUiState(
    val instances: List<Instance> = emptyList(),
    val downloadClients: List<DownloadClient> = emptyList(),
    val selectedInstanceIds: Set<Long> = emptySet(),
    val selectedDownloadClientIds: Set<Long> = emptySet(),
    val password: String = "",
    val includeInstancePreferences: Boolean = true,
    val includeTabPreferences: Boolean = true,
    val includeUiPreferences: Boolean = true,
    val isExporting: Boolean = false
) {
    constructor(): this(emptyList()) // default ios constructor
}