package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataResponse
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.firstOrNull

class AddMediaItemUseCase(
    private val instanceManager: InstanceManager,
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
            return
        }

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
