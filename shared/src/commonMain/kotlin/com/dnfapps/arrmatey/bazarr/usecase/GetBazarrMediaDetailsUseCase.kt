package com.dnfapps.arrmatey.bazarr.usecase

import com.dnfapps.arrmatey.bazarr.api.model.BazarrMedia
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMediaType
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class GetBazarrMediaDetailsUseCase(
    private val getBazarrRespositoryUseCase: GetBazarrInstanceRepositoryUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(id: Long, type: BazarrMediaType): Flow<BazarrMedia?> =
        getBazarrRespositoryUseCase.observeSelected()
            .filterNotNull()
            .flatMapLatest { repo ->
                when (type) {
                    BazarrMediaType.Movie -> repo.movies.map { result ->
                        (result as? NetworkResult.Success)?.data?.find { it.serviceId == id }
                    }
                    BazarrMediaType.Series -> repo.series.map { result ->
                        (result as? NetworkResult.Success)?.data?.find { it.serviceId == id }
                    }
                }

            }
}
