package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientRepository

class SetDownloadClientActiveUseCase(
    private val downloadClientRepository: DownloadClientRepository
) {
    suspend operator fun invoke(downloadClient: DownloadClient) {
        downloadClientRepository.setDownloadClientActive(downloadClient)
    }
}