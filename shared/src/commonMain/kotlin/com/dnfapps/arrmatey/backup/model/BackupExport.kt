package com.dnfapps.arrmatey.backup.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class BackupExport(
    val version: Int = 1,
    val instances: List<InstanceExport> = emptyList(),
    val downloadClients: List<DownloadClientExport> = emptyList(),
    val customWebpages: List<CustomWebpageExport> = emptyList(),
    val globalPreferences: GlobalPreferencesExport? = null,
    val mediaServers: List<MediaServerExport> = emptyList(),
    @Contextual val creationDateTime: Instant? = null,
)
