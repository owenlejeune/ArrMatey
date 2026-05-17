package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataResponse
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetAudiobookMetadataUseCase {
    operator fun invoke(
        asin: String,
        repository: ArrInstanceRepository
    ): Flow<AudiobookMetadataResponse?> = flow {
        val region = repository.listenarrConfiguration.value.defaultSearchRegion
        repository.getMetadata(asin, region)
            .onSuccess { emit(it) }
            .onError { _, _, _ -> emit(null) }
    }
}