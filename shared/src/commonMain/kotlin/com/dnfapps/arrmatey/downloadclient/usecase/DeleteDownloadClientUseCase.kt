package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientRepository

class DeleteDownloadClientUseCase(
    private val downloadClientRepository: DownloadClientRepository
) {
    suspend operator fun invoke(clientId: Long) {
        val downloadClient = downloadClientRepository.getDownloadClientById(clientId) ?: return
        downloadClientRepository.deleteDownloadClient(downloadClient)
    }
}
