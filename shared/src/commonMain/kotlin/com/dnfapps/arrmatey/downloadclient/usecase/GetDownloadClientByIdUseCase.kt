package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientRepository

class GetDownloadClientByIdUseCase(
    private val downloadClientRepository: DownloadClientRepository
) {
    suspend operator fun invoke(clientId: Long): DownloadClient? =
        downloadClientRepository.getDownloadClientById(clientId)
}