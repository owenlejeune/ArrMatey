package com.dnfapps.arrmatey.downloadclient.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientManager
import com.dnfapps.arrmatey.downloadclient.state.DownloadQueueState
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class ObserveDownloadQueueUseCase(
    private val downloadClientManager: DownloadClientManager
) {
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 0)

    operator fun invoke(pollingIntervalMillis: Long = 5000L): Flow<DownloadQueueState> = flow {
        var currentState: DownloadQueueState = DownloadQueueState.Initial
        emit(currentState)

        while (currentCoroutineContext().isActive) {
            val api = downloadClientManager.getSelectedDownloadClientApiSnapshot()

            if (api == null) {
                if (currentState !is DownloadQueueState.Success) {
                    emit(DownloadQueueState.NoClient)
                    currentState = DownloadQueueState.NoClient
                }
                delay(pollingIntervalMillis)
                continue
            }

            if (currentState is DownloadQueueState.Initial || currentState is DownloadQueueState.NoClient) {
                emit(DownloadQueueState.Loading)
                currentState = DownloadQueueState.Loading
            }

            val downloadsResult = api.getDownloads()
            val transferInfoResult = api.getTransferInfo()

            val newState = when (downloadsResult) {
                is NetworkResult.Success -> {
                    when (transferInfoResult) {
                        is NetworkResult.Success -> DownloadQueueState.Success(
                            items = downloadsResult.data,
                            transferInfo = transferInfoResult.data,
                            isRefreshing = false
                        )
                        is NetworkResult.Error -> {
                            currentState as? DownloadQueueState.Success
                                ?: DownloadQueueState.Error(
                                    code = transferInfoResult.code,
                                    message = transferInfoResult.message,
                                    cause = transferInfoResult.cause
                                )
                        }
                        is NetworkResult.Loading -> currentState
                    }
                }
                is NetworkResult.Error -> {
                    currentState as? DownloadQueueState.Success
                        ?: DownloadQueueState.Error(
                            code = downloadsResult.code,
                            message = downloadsResult.message,
                            cause = downloadsResult.cause
                        )
                }
                is NetworkResult.Loading -> currentState
            }

            if (newState != currentState) {
                emit(newState)
                currentState = newState
            }

            delay(pollingIntervalMillis)
        }
    }

    suspend fun triggerRefresh() {
        refreshTrigger.emit(Unit)
    }
}