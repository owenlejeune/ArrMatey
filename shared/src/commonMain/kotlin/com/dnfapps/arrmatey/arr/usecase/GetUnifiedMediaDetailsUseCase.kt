package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.state.MediaDetailsUiState
import com.dnfapps.arrmatey.bazarr.state.BazarrDetails
import com.dnfapps.arrmatey.bazarr.usecase.GetBazarrEpisodesUseCase
import com.dnfapps.arrmatey.bazarr.usecase.GetBazarrMediaDetailsUseCase
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.CombinedRatings
import com.dnfapps.arrmatey.seerr.state.SeerrDetailsState
import com.dnfapps.arrmatey.seerr.usecase.GetSeerrMediaDetailsUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSeerrMovieRatingsUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSeerrTvRatingsUseCase
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetUnifiedMediaDetailsUseCase(
    private val getMediaDetailsUseCase: GetMediaDetailsUseCase,
    private val getSeerrMediaDetailsUseCase: GetSeerrMediaDetailsUseCase,
    private val getBazarrMediaDetailsUseCase: GetBazarrMediaDetailsUseCase,
    private val getBazarrEpisodesUseCase: GetBazarrEpisodesUseCase,
    private val getSeerrMovieRatingsUseCase: GetSeerrMovieRatingsUseCase,
    private val getSeerrTvRatingsUseCase: GetSeerrTvRatingsUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        arrId: Long? = null,
        tmdbId: Long? = null,
        tvdbId: Long? = null,
        instanceType: InstanceType? = null,
        requestType: RequestType? = null,
        arrRepository: ArrInstanceRepository? = null,
        seerrRepository: SeerrInstanceRepository? = null,
        bazarrRepository: BazarrInstanceRepository? = null
    ): Flow<UnifiedMediaDetailsUiState> {
        val arrFlow = if (arrRepository != null) {
            if (arrId != null) {
                getMediaDetailsUseCase(arrId, arrRepository.instance.id)
            } else if (tmdbId != null || tvdbId != null) {
                val query = tmdbId?.let { "tmdb:$it" } ?: "tvdb:$tvdbId"
                flowOf(query).flatMapLatest { q ->
                    repositoryLookupFlow(arrRepository, q)
                }
            } else flowOf(MediaDetailsUiState.Initial)
        } else flowOf(MediaDetailsUiState.Initial)

        return arrFlow.flatMapLatest { arrState ->
            val targetItem = (arrState as? MediaDetailsUiState.Success)?.item

            val resolvedTmdbId = tmdbId ?: when (targetItem) {
                is ArrMovie -> targetItem.tmdbId.takeIf { it > 0 }
                is ArrSeries -> targetItem.tmdbId?.takeIf { it > 0 }
                else -> null
            }

            val resolvedRequestType = requestType ?: when (targetItem) {
                is ArrMovie -> RequestType.Movie
                is ArrSeries -> RequestType.Tv
                else -> null
            }

            val seerrAndRatingsFlow: Flow<Pair<SeerrDetailsState, CombinedRatings?>> = if (resolvedTmdbId != null && resolvedRequestType != null) {
                getSeerrAndRatingsFlow(resolvedTmdbId, resolvedRequestType, seerrRepository)
            } else if (seerrRepository != null && targetItem is ArrSeries) {
                // If it's a TV series without explicit TMDB ID, attempt lookup via Seerr search
                val query = targetItem.cleanTitle ?: targetItem.title ?: ""
                flow {
                    val searchResult = seerrRepository.client.search(query)
                    val foundId = (searchResult as? NetworkResult.Success)?.data?.results?.firstOrNull {
                        it.mediaType == RequestType.Tv
                    }?.id
                    emit(foundId)
                }.flatMapLatest { searchedTmdbId ->
                    if (searchedTmdbId != null) {
                        getSeerrAndRatingsFlow(searchedTmdbId, RequestType.Tv, seerrRepository)
                    } else {
                        flowOf(SeerrDetailsState.Initial to null)
                    }
                }
            } else {
                getSeerrAndRatingsFlow(tmdbId, requestType, seerrRepository)
            }

            val bazarrFlow = flowOf(BazarrDetails())

            combine(seerrAndRatingsFlow, bazarrFlow) { (seerrState, ratings), bazarr ->
                if (seerrState is SeerrDetailsState.Loading && arrState !is MediaDetailsUiState.Success) {
                    return@combine UnifiedMediaDetailsUiState.Loading
                }
                if (arrState is MediaDetailsUiState.Loading && seerrState !is SeerrDetailsState.Success) {
                    return@combine UnifiedMediaDetailsUiState.Loading
                }

                if (seerrState is SeerrDetailsState.Error && arrState !is MediaDetailsUiState.Success) {
                    return@combine UnifiedMediaDetailsUiState.Error(seerrState.message)
                }
                if (arrState is MediaDetailsUiState.Error && seerrState !is SeerrDetailsState.Success) {
                    return@combine UnifiedMediaDetailsUiState.Error(arrState.message)
                }

                UnifiedMediaDetailsUiState.Success(
                    arrMedia = (arrState as? MediaDetailsUiState.Success)?.item,
                    seerrMedia = (seerrState as? SeerrDetailsState.Success)?.item,
                    bazarrMedia = bazarr,
                    rtRatings = (seerrState as? SeerrDetailsState.Success)?.rtRatings ?: ratings?.rt,
                    imdbRatings = (seerrState as? SeerrDetailsState.Success)?.imdbRatings ?: ratings?.imdb,
                    episodes = (arrState as? MediaDetailsUiState.Success)?.episodes ?: emptyList(),
                    albums = (arrState as? MediaDetailsUiState.Success)?.albums ?: emptyList(),
                    tracks = (arrState as? MediaDetailsUiState.Success)?.tracks ?: emptyMap(),
                    trackFiles = (arrState as? MediaDetailsUiState.Success)?.trackFiles ?: emptyMap(),
                    bookSeries = (arrState as? MediaDetailsUiState.Success)?.bookSeries ?: emptyList(),
                    bookFiles = (arrState as? MediaDetailsUiState.Success)?.bookFiles ?: emptyList(),
                    books = (arrState as? MediaDetailsUiState.Success)?.books ?: emptyList(),
                    extraFiles = (arrState as? MediaDetailsUiState.Success)?.extraFiles ?: emptyList(),
                    isMonitored = (arrState as? MediaDetailsUiState.Success)?.item?.monitored ?: false
                )
            }
        }
    }

    private fun getSeerrAndRatingsFlow(
        targetTmdbId: Long?,
        targetRequestType: RequestType?,
        seerrRepository: SeerrInstanceRepository?
    ): Flow<Pair<SeerrDetailsState, CombinedRatings?>> {
        if (seerrRepository == null || targetTmdbId == null || targetRequestType == null) {
            return flowOf(SeerrDetailsState.Initial to null)
        }

        val seerrFlow = getSeerrMediaDetailsUseCase(targetTmdbId, targetRequestType, seerrRepository)
        val ratingsFlow: Flow<CombinedRatings?> = if (targetRequestType == RequestType.Movie) {
            getSeerrMovieRatingsUseCase(targetTmdbId)
        } else if (targetRequestType == RequestType.Tv) {
            getSeerrTvRatingsUseCase(targetTmdbId).map { rtRating ->
                rtRating?.let { CombinedRatings(rt = it, imdb = null) }
            }
        } else flowOf(null)

        return combine(seerrFlow, ratingsFlow) { seerrState, ratings ->
            seerrState to ratings
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun repositoryLookupFlow(repository: ArrInstanceRepository, query: String): Flow<MediaDetailsUiState> {
        return flowOf(Unit).map {
            repository.performLookup(query)
        }.flatMapLatest {
            repository.lookupResults.flatMapLatest { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val item = result.data.firstOrNull()
                        if (item?.id != null && item.id != 0L) {
                            getMediaDetailsUseCase(item.id!!, repository.instance.id)
                        } else {
                            val targetItem = item ?: return@flatMapLatest flowOf(MediaDetailsUiState.Initial)
                            flowOf(MediaDetailsUiState.Success(item = targetItem))
                        }
                    }
                    is NetworkResult.Error -> flowOf(MediaDetailsUiState.Error(result.message))
                    is NetworkResult.Loading -> flowOf(MediaDetailsUiState.Loading)
                    null -> flowOf(MediaDetailsUiState.Initial)
                }
            }
        }
    }
}
