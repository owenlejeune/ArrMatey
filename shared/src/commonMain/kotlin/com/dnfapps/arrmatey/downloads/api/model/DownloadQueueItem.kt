package com.dnfapps.arrmatey.downloads.api.model

import kotlinx.serialization.Serializable

@Serializable
data class DownloadQueueItem(
    val id: String,
    val name: String,
    val size: Long,
    val progress: Double,
    val status: String,
    val speed: Long,
    val eta: Long?,
    val seeds: Int? = null,
    val peers: Int? = null,
    val category: String? = null
)
