package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TestDownloadClientConnectionUseCase(
    private val downloadClientManager: DownloadClientManager
) {
    operator fun invoke(id: Long, forceRefresh: Boolean = false): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)

        val api = if (forceRefresh) {
            downloadClientManager.refreshApi(id)
        } else {
            downloadClientManager.getOrCreateApi(id)
        }

        if (api == null) {
            emit(OperationStatus.Error(message = "Download client not found in database"))
            return@flow
        }

        when (val result = api.testConnection()) {
            is NetworkResult.Success -> emit(OperationStatus.Success())
            is NetworkResult.Error -> emit(
                OperationStatus.Error(
                    code = result.code,
                    message = result.message ?: "Connection test failed",
                    cause = result.cause
                )
            )
            is NetworkResult.Loading -> emit(OperationStatus.InProgress)
        }
    }

    operator fun invoke(client: DownloadClient): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)

        val api = downloadClientManager.createApiFromClient(client)

        when (val result = api.testConnection()) {
            is NetworkResult.Success -> emit(OperationStatus.Success())
            is NetworkResult.Error -> emit(
                OperationStatus.Error(
                    code = result.code,
                    message = result.message ?: "Connection test failed",
                    cause = result.cause
                )
            )
            is NetworkResult.Loading -> emit(OperationStatus.InProgress)
        }
    }
}