package com.dnfapps.arrmatey.downloads.api.model

import kotlinx.serialization.Serializable

@Serializable
data class DownloadQueueItem(
    val id: String,
    val name: String,
    val size: Long,
    val progress: Double, // 0.0 to 1.0
    val status: String,
    val speed: Long, // bytes per second
    val eta: Long?, // seconds
    val seeds: Int? = null,
    val peers: Int? = null,
    val category: String? = null
)

@Serializable
data class DownloadClientStatus(
    val dlSpeed: Long,
    val upSpeed: Long,
    val isPaused: Boolean
)
