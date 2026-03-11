package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ResumeDownloadUseCase(
    private val downloadClientManager: DownloadClientManager
) {

    operator fun invoke(id: String): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)

        val api = downloadClientManager.getSelectedDownloadClientApiSnapshot()
        if (api == null) {
            emit(OperationStatus.Error(message = "No download client selected"))
            return@flow
        }

        when (val result = api.resumeDownload(id)) {
            is NetworkResult.Success -> emit(OperationStatus.Success("Download resumed"))
            is NetworkResult.Error -> emit(OperationStatus.Error(result.code, result.message, result.cause))
            is NetworkResult.Loading -> emit(OperationStatus.InProgress)
            else -> emit(OperationStatus.Error(message = "Unknown response"))
        }
    }
}
