package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataResponse
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.flow.firstOrNull

class AddMediaItemUseCase(
    private val instanceManager: InstanceManager,
    private val logger: Logger,
) {
    suspend operator fun invoke(
        instanceType: InstanceType,
        item: ArrMedia,
        metadata: AudiobookMetadataResponse? = null,
        searchOnAdd: Boolean = false,
        targetInstanceId: Long? = null,
    ) {
        val repository =
            if (targetInstanceId != null) {
                instanceManager.getArrRepository(targetInstanceId)
            } else {
                instanceManager.getSelectedArrRepository(instanceType).firstOrNull()
            }

        if (repository == null) {
            logger.error {
                "AddMediaItemUseCase: no repository resolved (type=$instanceType, targetInstanceId=$targetInstanceId); " +
                    "add aborted for item '${item.title}'"
            }
            return
        }

        invoke(instanceType, repository, item, metadata, searchOnAdd)
    }

    suspend operator fun invoke(
        instanceType: InstanceType,
        repository: ArrInstanceRepository,
        item: ArrMedia,
        metadata: AudiobookMetadataResponse? = null,
        searchOnAdd: Boolean = false,
    ) {
        if (
            instanceType == InstanceType.Listenarr &&
            metadata != null &&
            item is SearchAudiobook
        ) {
            val body = metadata.metadata.toBody(metadata.source)
            repository.addNewAudiobook(item, body, searchOnAdd)
        } else {
            repository.addItem(item, searchOnAdd)
        }
    }
}
