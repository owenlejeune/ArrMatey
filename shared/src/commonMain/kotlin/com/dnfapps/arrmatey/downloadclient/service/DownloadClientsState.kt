package com.dnfapps.arrmatey.downloadclient.service

import com.dnfapps.arrmatey.downloadclient.model.DownloadClient

class DownloadClientsState(
    val downloadClients: List<DownloadClient> = emptyList(),
    val selectedDownloadClient: DownloadClient? = null
) {
    constructor(): this(downloadClients = emptyList()) // ios empty constructor
}