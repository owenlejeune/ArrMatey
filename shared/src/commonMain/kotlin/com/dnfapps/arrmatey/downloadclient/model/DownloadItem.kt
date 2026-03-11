package com.dnfapps.arrmatey.downloadclient.model

import com.dnfapps.arrmatey.compose.utils.toFormattedDuration

data class DownloadItem(
    val client: DownloadClient,
    val id: String,
    val name: String,
    val size: Long,
    val progress: Double,
    val downloadSpeed: Long,
    val uploadSpeed: Long,
    val status: DownloadItemStatus,
    val category: String,
    val addedOn: Long,
    val eta: Long,
    val etaString: String = eta.toFormattedDuration()
)
