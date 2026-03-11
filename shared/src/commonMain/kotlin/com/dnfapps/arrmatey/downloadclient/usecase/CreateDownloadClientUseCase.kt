package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.downloadclient.database.DownloadClientInsertResult
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientRepository
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientMutationState

class CreateDownloadClientUseCase(
    private val downloadClientRepository: DownloadClientRepository
) {
    suspend operator fun invoke(downloadClient: DownloadClient): DownloadClientMutationState {
        return when (val result = downloadClientRepository.createDownloadClient(downloadClient)) {
            is DownloadClientInsertResult.Success -> DownloadClientMutationState.Success(result.id)
            is DownloadClientInsertResult.Conflict -> DownloadClientMutationState.Conflict(result.fields)
            is DownloadClientInsertResult.Error -> DownloadClientMutationState.Error(result.message)
            else -> DownloadClientMutationState.Error("Unexpected create result")
        }
    }
}
