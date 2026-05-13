package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.AudiobookPreviewPaths
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull

class GetAudiobookPreviewPathUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(preview: ArrMedia): Flow<String> = channelFlow {
        if (preview !is SearchAudiobook) {
            send("")
        } else {
            instanceManager.getSelectedArrRepository(InstanceType.Listenarr)
                .firstOrNull()
                ?.getPreviewPath(preview)
                ?.onSuccess {
                    send(it.relativePath)
                }
        }
    }
//    suspend operator fun invoke(preview: ArrMedia): NetworkResult<AudiobookPreviewPaths> {
//        if (preview !is SearchAudiobook)
//            return NetworkResult.Error(message = "Not a SearchAudiobook")
//        return instanceManager.getSelectedArrRepository(InstanceType.Listenarr)
//            .firstOrNull()
//            ?.getPreviewPath(preview)
//            ?: NetworkResult.Error(message = "Listenarr repository not found")
//    }
}
