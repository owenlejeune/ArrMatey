package com.dnfapps.arrmatey.downloadclient.model

import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.instances.model.UpdateInfo

data class DownloadClientSystemOverview(
    val version: String? = null,
    val webApiVersion: String? = null,
    val downloadDir: String? = null,
    val diskSpaces: List<ArrDiskSpace> = emptyList(),
    val connectionStatus: String? = null,
    val dhtNodes: Long? = null,
    val totalPeers: Long? = null,
    val listenPort: Long? = null,
    val transferInfo: DownloadTransferInfo? = null,
    val activeDownloadsCount: Int = 0,
    val alltimeDownloaded: Long? = null,
    val alltimeUploaded: Long? = null,
    val extraInfo: List<Pair<String, String>> = emptyList(),
    val updateInfo: UpdateInfo? = null,
)
