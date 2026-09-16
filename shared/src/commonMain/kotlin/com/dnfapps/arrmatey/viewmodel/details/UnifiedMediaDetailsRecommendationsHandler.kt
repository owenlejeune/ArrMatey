package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.seerr.usecase.GetRecommendationsUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetSimilarUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class UnifiedMediaDetailsRecommendationsHandler(
    private val getRecommendationsUseCase: GetRecommendationsUseCase,
    private val getSimilarUseCase: GetSimilarUseCase,
) {
    private var recommendationsPagingController: PagingController<DiscoverResult>? = null
    private var similarPagingController: PagingController<DiscoverResult>? = null

    private val _recommendationsState = MutableStateFlow(PagedData<DiscoverResult>())
    val recommendationsState: StateFlow<PagedData<DiscoverResult>> = _recommendationsState.asStateFlow()

    private val _similarState = MutableStateFlow(PagedData<DiscoverResult>())
    val similarState: StateFlow<PagedData<DiscoverResult>> = _similarState.asStateFlow()

    fun observeRecommendations(
        scope: CoroutineScope,
        seerrRepoFlow: Flow<SeerrInstanceRepository?>,
        uiStateFlow: Flow<UnifiedMediaDetailsUiState>,
        initialTmdbId: Long?,
        initialRequestType: RequestType?,
    ) {
        scope.launch {
            combine(seerrRepoFlow, uiStateFlow) { repo, state ->
                if (repo == null) return@combine null

                val success = state as? UnifiedMediaDetailsUiState.Success
                val targetItem = success?.arrMedia

                val resolvedTmdbId =
                    initialTmdbId ?: when (targetItem) {
                        is ArrMovie -> targetItem.tmdbId.takeIf { it > 0 }
                        is ArrSeries -> targetItem.tmdbId?.takeIf { it > 0 }
                        else -> null
                    } ?: (success?.seerrMedia as? MovieDetails)?.id
                        ?: (success?.seerrMedia as? TvDetails)?.id

                val resolvedReqType =
                    initialRequestType ?: when (targetItem) {
                        is ArrMovie -> RequestType.Movie
                        is ArrSeries -> RequestType.Tv
                        else -> null
                    } ?: when (success?.seerrMedia) {
                        is MovieDetails -> RequestType.Movie
                        is TvDetails -> RequestType.Tv
                        else -> null
                    }

                if (resolvedTmdbId != null &&
                    resolvedTmdbId > 0 &&
                    (resolvedReqType == RequestType.Movie || resolvedReqType == RequestType.Tv)
                ) {
                    Triple(repo, resolvedTmdbId, resolvedReqType)
                } else {
                    null
                }
            }.distinctUntilChanged { old, new ->
                if (old == null && new == null) {
                    true
                } else if (old == null || new == null) {
                    false
                } else {
                    old.first.instance.id == new.first.instance.id && old.second == new.second && old.third == new.third
                }
            }.collectLatest { target ->
                if (target != null) {
                    val (repo, tmdbId, requestType) = target
                    val recController = getRecommendationsUseCase.createPagingController(repo, requestType, tmdbId, scope)
                    recommendationsPagingController = recController
                    val simController = getSimilarUseCase.createPagingController(repo, requestType, tmdbId, scope)
                    similarPagingController = simController

                    recController.loadInitialPage()
                    simController.loadInitialPage()

                    launch {
                        recController.state.collect { _recommendationsState.value = it }
                    }
                    launch {
                        simController.state.collect { _similarState.value = it }
                    }
                } else {
                    recommendationsPagingController = null
                    similarPagingController = null
                    _recommendationsState.value = PagedData()
                    _similarState.value = PagedData()
                }
            }
        }
    }

    fun loadNextRecommendationsPage() {
        recommendationsPagingController?.loadNextPage()
    }

    fun loadNextSimilarPage() {
        similarPagingController?.loadNextPage()
    }

    fun refresh() {
        recommendationsPagingController?.refresh()
        similarPagingController?.refresh()
    }
}
