package com.dnfapps.arrmatey.downloads.api.model

import kotlinx.serialization.Serializable

@Serializable
data class DownloadClientStatus(
    val dlSpeed: Long,
    val upSpeed: Long,
    val isPaused: Boolean
)
