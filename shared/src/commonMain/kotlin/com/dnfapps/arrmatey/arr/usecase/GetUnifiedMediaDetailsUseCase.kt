package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.SonarrQueueItem
import com.dnfapps.arrmatey.arr.state.MediaDetailsUiState
import com.dnfapps.arrmatey.bazarr.state.BazarrDetails
import com.dnfapps.arrmatey.bazarr.usecase.GetBazarrEpisodesUseCase
import com.dnfapps.arrmatey.bazarr.usecase.GetBazarrMediaDetailsUseCase
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.model.EpisodeWrapper
import com.dnfapps.arrmatey.model.SeasonWrapper
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.CombinedRatings
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
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
    private val getSeerrTvRatingsUseCase: GetSeerrTvRatingsUseCase,
    private val getActivityTasksUseCase: GetActivityTasksUseCase
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
        val arrFlow: Flow<MediaDetailsUiState> = if (arrRepository != null) {
            if (arrId != null) {
                getMediaDetailsUseCase(arrId, arrRepository.instance.id)
            } else if (tmdbId != null || tvdbId != null) {
                val query = tmdbId?.let { "tmdb:$it" } ?: "tvdb:$tvdbId"
                repositoryLookupFlow(arrRepository, query)
            } else {
                flowOf(MediaDetailsUiState.Initial)
            }
        } else {
            flowOf(MediaDetailsUiState.Initial)
        }

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
            } else if (tmdbId != null && requestType != null) {
                getSeerrAndRatingsFlow(tmdbId, requestType, seerrRepository)
            } else {
                flowOf(SeerrDetailsState.Initial to null)
            }

            val activityTasksFlow = getActivityTasksUseCase()
            val bazarrFlow = flowOf(BazarrDetails())

            combine(
                seerrAndRatingsFlow,
                activityTasksFlow,
                bazarrFlow
            ) { (seerrState, ratings), activityTasks, bazarr ->
                buildUnifiedState(arrState, seerrState, ratings, activityTasks, bazarr)
            }
        }
    }

    private fun buildUnifiedState(
        arrState: MediaDetailsUiState,
        seerrState: SeerrDetailsState,
        ratings: CombinedRatings?,
        activityTasks: List<QueueItem>,
        bazarr: BazarrDetails
    ): UnifiedMediaDetailsUiState {
        if (arrState is MediaDetailsUiState.Loading || seerrState is SeerrDetailsState.Loading) {
            return UnifiedMediaDetailsUiState.Loading
        }

        val isArrSuccess = arrState is MediaDetailsUiState.Success
        val isSeerrSuccess = seerrState is SeerrDetailsState.Success

        if (!isArrSuccess && !isSeerrSuccess) {
            if (arrState is MediaDetailsUiState.Error && seerrState is SeerrDetailsState.Error) {
                return UnifiedMediaDetailsUiState.Error(arrState.message ?: seerrState.message)
            }
            if (arrState is MediaDetailsUiState.Error) {
                return UnifiedMediaDetailsUiState.Error(arrState.message)
            }
            if (seerrState is SeerrDetailsState.Error) {
                return UnifiedMediaDetailsUiState.Error(seerrState.message)
            }
            return UnifiedMediaDetailsUiState.Loading
        }

        val arrSuccess = arrState as? MediaDetailsUiState.Success
        val seerrSuccess = seerrState as? SeerrDetailsState.Success

        val arrSeries = arrSuccess?.item as? ArrSeries
        val arrSeasons = arrSeries?.seasons ?: emptyList()
        val arrEpisodes = arrSuccess?.episodes ?: emptyList()

        val seerrTv = seerrSuccess?.item as? TvDetails
        val seerrSeasons = seerrTv?.seasons ?: emptyList()
        val seerrEpisodes = seerrSeasons.flatMap { it.episodes }

        val allSeasonNumbers = (arrSeasons.map { it.seasonNumber } + seerrSeasons.map { it.seasonNumber }).distinct().sortedDescending()
        val arrSeasonMap = arrSeasons.associateBy { it.seasonNumber }
        val seerrSeasonMap = seerrSeasons.associateBy { it.seasonNumber }
        val arrEpMap = arrEpisodes.groupBy { it.seasonNumber }
        val seerrEpMap = seerrEpisodes.groupBy { it.seasonNumber }

        val sonarrTasks = activityTasks.filterIsInstance<SonarrQueueItem>()

        val combinedSeasons = allSeasonNumbers.map { seasonNumber ->
            val arrSeason = arrSeasonMap[seasonNumber]
            val seerrSeason = seerrSeasonMap[seasonNumber]
            val seasonArrEps = arrEpMap[seasonNumber] ?: emptyList()
            val seasonSeerrEps = seerrEpMap[seasonNumber] ?: emptyList()

            val allEpNumbers = (seasonArrEps.map { it.episodeNumber } + seasonSeerrEps.map { it.episodeNumber }).distinct().sortedDescending()
            val seasonArrEpMap = seasonArrEps.associateBy { it.episodeNumber }
            val seasonSeerrEpMap = seasonSeerrEps.associateBy { it.episodeNumber }

            val seasonEpisodes = allEpNumbers.map { epNum ->
                val arrEp = seasonArrEpMap[epNum]
                val seerrEp = seasonSeerrEpMap[epNum]

                val queueItem = arrEp?.let { ep ->
                    sonarrTasks.firstOrNull { it.calcEpisodeId == ep.id }
                        ?: sonarrTasks.firstOrNull {
                            it.calcSeriesId == ep.seriesId &&
                                it.seasonNumber == ep.seasonNumber &&
                                it.calcEpisodeId == null
                        }
                }

                EpisodeWrapper(
                    arrEpisode = arrEp,
                    seerrEpisode = seerrEp,
                    isActive = queueItem != null,
                    activityProgress = queueItem?.progressLabel
                )
            }

            SeasonWrapper(
                seasonNumber = seasonNumber,
                arrSeason = arrSeason,
                seerrSeason = seerrSeason,
                episodes = seasonEpisodes
            )
        }

        val combinedEpisodes = combinedSeasons.flatMap { it.episodes }

        return UnifiedMediaDetailsUiState.Success(
            arrMedia = arrSuccess?.item,
            seerrMedia = seerrSuccess?.item,
            bazarrMedia = bazarr,
            rtRatings = seerrSuccess?.rtRatings ?: ratings?.rt,
            imdbRatings = seerrSuccess?.imdbRatings ?: ratings?.imdb,
            seasons = combinedSeasons,
            episodes = combinedEpisodes,
            albums = arrSuccess?.albums ?: emptyList(),
            tracks = arrSuccess?.tracks ?: emptyMap(),
            trackFiles = arrSuccess?.trackFiles ?: emptyMap(),
            bookSeries = arrSuccess?.bookSeries ?: emptyList(),
            bookFiles = arrSuccess?.bookFiles ?: emptyList(),
            books = arrSuccess?.books ?: emptyList(),
            extraFiles = arrSuccess?.extraFiles ?: emptyList(),
            isMonitored = arrSuccess?.item?.monitored ?: false
        )
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
        } else {
            flowOf(null)
        }

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
