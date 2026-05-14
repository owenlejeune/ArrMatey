package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
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
        preview: ArrMedia
    ): Flow<String> = channelFlow {
        if (preview !is SearchAudiobook) {
            send("")
            return@channelFlow
        }

        val instance = instanceManager.getSelectedArrRepository(InstanceType.Listenarr)
            .firstOrNull()

        val region = instance?.listenarrConfiguration?.value?.defaultSearchRegion ?: "us"
        instance?.getMetadata(preview.asin, region)
            ?.onSuccess { (source, _, metadata) ->
                val body = metadata.toBody(source)
                instance.getPreviewPath(rootPath, body)
                    .onSuccess { (_, relativePath, _) ->
                        send(relativePath)
                    }
                    .onError { _, _, _ ->
                        send("")
                    }
            }
            ?.onError { _, _, _ ->
                send("")
            }
//        if (preview !is SearchAudiobook) {
//            send("")
//        } else {
//            instanceManager.getSelectedArrRepository(InstanceType.Listenarr)
//                .firstOrNull()
//                ?.getPreviewPath(rootPath, preview)
//                ?.onSuccess {
//                    send(it.relativePath)
//                }?.onError { code, message, cause ->
//                    println("$code - $message - ${cause?.printStackTrace()}")
//                }
//        }
    }

}
