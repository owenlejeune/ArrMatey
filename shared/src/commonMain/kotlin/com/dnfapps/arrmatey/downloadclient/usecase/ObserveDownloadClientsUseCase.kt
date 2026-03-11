package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientRepository
import kotlinx.coroutines.flow.Flow

class ObserveDownloadClientsUseCase(
    private val downloadClientRepository: DownloadClientRepository
) {
    operator fun invoke(): Flow<List<DownloadClient>> =
        downloadClientRepository.observeAllDownloadClients()
}
