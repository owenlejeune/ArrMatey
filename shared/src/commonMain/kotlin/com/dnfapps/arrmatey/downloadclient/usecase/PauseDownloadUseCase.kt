package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PauseDownloadUseCase(
    private val downloadClientManager: DownloadClientManager,
    private val logger: Logger
) {

    operator fun invoke(clientId: Long, ids: List<String>): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)

        val api = downloadClientManager.getOrCreateApi(clientId)
        if (api == null) {
            logger.error { "Pause failed: no download-client API for id $clientId" }
            emit(OperationStatus.Error(message = "Download client not available"))
            return@flow
        }

        when (val result = api.pauseDownload(ids)) {
            is NetworkResult.Success -> emit(OperationStatus.Success("Downloads paused"))
            is NetworkResult.Error -> {
                logger.error(result.cause) {
                    "Pause failed on client $clientId (ids=$ids): ${result.message} (code=${result.code})"
                }
                emit(OperationStatus.Error(result.code, result.message, result.cause))
            }
            is NetworkResult.Loading -> emit(OperationStatus.InProgress)
        }
    }
}
