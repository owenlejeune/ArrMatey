package com.dnfapps.arrmatey.bazarr.usecase

import com.dnfapps.arrmatey.bazarr.api.model.BazarrEpisode
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class GetBazarrEpisodesUseCase(
    private val getBazarrRespositoryUseCase: GetBazarrInstanceRepositoryUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(sonarrSeriesId: Long): Flow<List<BazarrEpisode>> =
        getBazarrRespositoryUseCase.observeSelected()
            .filterNotNull()
            .flatMapLatest { repo ->
                repo.getEpisodes(sonarrSeriesId)
                repo.episodes.map { it[sonarrSeriesId] ?: emptyList() }
            }
}
