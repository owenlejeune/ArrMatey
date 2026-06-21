package com.dnfapps.arrmatey.backup.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupExport(
    val version: Int = 1,
    val instances: List<InstanceExport> = emptyList(),
    val downloadClients: List<DownloadClientExport> = emptyList(),
    val globalPreferences: GlobalPreferencesExport? = null
)
