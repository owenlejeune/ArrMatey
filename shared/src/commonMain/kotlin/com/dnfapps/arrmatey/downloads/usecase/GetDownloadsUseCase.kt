package com.dnfapps.arrmatey.downloads.usecase

import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.downloads.state.DownloadsState
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class GetDownloadsUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(instanceId: Long): Flow<DownloadsState> = flow {
        val repository = instanceManager.getDownloadRepository(instanceId)
            ?: return@flow emit(DownloadsState.Error("Instance not found", ErrorType.Unexpected))

        repository.refresh()
        emitAll(repository.queue)
    }
}
