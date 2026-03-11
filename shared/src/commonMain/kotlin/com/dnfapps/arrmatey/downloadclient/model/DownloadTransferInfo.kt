package com.dnfapps.arrmatey.downloadclient.model

data class DownloadTransferInfo(
    val client: DownloadClient,
    val downloadSpeed: Long,
    val uploadSpeed: Long
)
