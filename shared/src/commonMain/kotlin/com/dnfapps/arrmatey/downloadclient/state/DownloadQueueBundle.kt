package com.dnfapps.arrmatey.downloadclient.state

import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadTransferInfo

data class DownloadQueueBundle(
    val queueItems: List<DownloadItem> = emptyList(),
    val transferInfo: List<DownloadTransferInfo> = emptyList()
) {
    constructor(): this(queueItems = emptyList()) // ios empty constructor
}