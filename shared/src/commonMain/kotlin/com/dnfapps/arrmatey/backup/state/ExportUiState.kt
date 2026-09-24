package com.dnfapps.arrmatey.backup.state

import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.webpage.model.CustomWebpage

data class ExportUiState(
    val instances: List<Instance> = emptyList(),
    val downloadClients: List<DownloadClient> = emptyList(),
    val customWebpages: List<CustomWebpage> = emptyList(),
    val selectedInstanceIds: Set<Long> = emptySet(),
    val selectedDownloadClientIds: Set<Long> = emptySet(),
    val selectedCustomWebpageIds: Set<Long> = emptySet(),
    val password: String = "",
    val includeInstancePreferences: Boolean = true,
    val includeTabPreferences: Boolean = true,
    val includeUiPreferences: Boolean = true,
    val includeIntegrationsPreferences: Boolean = true,
    val isExporting: Boolean = false,
) {
    constructor() : this(emptyList()) // default ios constructor
}
