package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataResponse
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull

class GetAudiobookPreviewPathUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(
        rootPath: String,
        metadataResponse: AudiobookMetadataResponse
    ): Flow<String> = channelFlow {
        val repository = instanceManager.getSelectedArrRepository(InstanceType.Listenarr)
            .firstOrNull()

        if (repository == null) {
            send("")
            return@channelFlow
        }

        val (source, _, metadata) = metadataResponse
        val body = metadata.toBody(source)
        repository.getPreviewPath(rootPath, body)
            .onSuccess { (_, relativePath, _) ->
                send(relativePath)
            }
            .onError { _, _, _ ->
                send("")
            }
    }

}
