package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.NetworkResult
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeleteDownloadUseCase(
    private val downloadClientManager: DownloadClientManager,
    private val logger: Logger,
) {
    operator fun invoke(
        clientId: Long,
        ids: List<String>,
        deleteFiles: Boolean,
    ): Flow<OperationStatus> =
        flow {
            emit(OperationStatus.InProgress)

            val api = downloadClientManager.getOrCreateApi(clientId)
            if (api == null) {
                logger.error { "Delete failed: no download-client API for id $clientId" }
                emit(OperationStatus.Error(message = "Download client not available"))
                return@flow
            }

            when (val result = api.deleteDownload(ids, deleteFiles)) {
                is NetworkResult.Success -> emit(OperationStatus.Success("Downloads deleted"))
                is NetworkResult.Error -> {
                    logger.error(result.cause) {
                        "Delete failed on client $clientId (ids=$ids): ${result.message} (code=${result.code})"
                    }
                    emit(OperationStatus.Error(result.code, result.message, result.cause))
                }
                is NetworkResult.Loading -> emit(OperationStatus.InProgress)
            }
        }
}
