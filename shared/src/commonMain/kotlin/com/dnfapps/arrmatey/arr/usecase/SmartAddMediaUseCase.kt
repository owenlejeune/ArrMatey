package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataResponse
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository

class SmartAddMediaUseCase(
    private val addMediaItemUseCase: AddMediaItemUseCase,
) {
    suspend operator fun invoke(
        instanceType: InstanceType,
        item: ArrMedia,
        metadata: AudiobookMetadataResponse? = null,
        searchOnAdd: Boolean = false,
        targetInstanceId: Long? = null,
    ) {
        addMediaItemUseCase(
            instanceType = instanceType,
            item = item,
            metadata = metadata,
            searchOnAdd = searchOnAdd,
            targetInstanceId = targetInstanceId,
        )
    }

    suspend operator fun invoke(
        instanceType: InstanceType,
        repository: ArrInstanceRepository,
        item: ArrMedia,
        metadata: AudiobookMetadataResponse? = null,
        searchOnAdd: Boolean = false,
    ) {
        addMediaItemUseCase(
            instanceType = instanceType,
            repository = repository,
            item = item,
            metadata = metadata,
            searchOnAdd = searchOnAdd,
        )
    }
}
