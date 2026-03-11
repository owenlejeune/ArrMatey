package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager

class RefreshDownloadQueueUseCase(
    private val downloadClientManager: DownloadClientManager
) {
    suspend operator fun invoke() {
        val api = downloadClientManager.getSelectedDownloadClientApiSnapshot()
        api?.getDownloads()
    }
}