package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.model.TracearrMediaUiState
import com.dnfapps.arrmatey.model.TracearrStatsWindowType
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UnifiedMediaDetailsTracearrHandler {
    private val _tracearrState = MutableStateFlow(TracearrMediaUiState())
    val tracearrState: StateFlow<TracearrMediaUiState> = _tracearrState.asStateFlow()

    private var currentTracearrRef: String? = null

    fun observeTracearrData(
        scope: CoroutineScope,
        uiStateFlow: Flow<UnifiedMediaDetailsUiState>,
        getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
        preferencesStore: PreferencesStore,
        initialTmdbId: Long?,
        initialRequestType: RequestType?,
    ) {
        scope.launch {
            combine(
                uiStateFlow,
                getTracearrInstanceRepositoryUseCase.observeSelected(),
                preferencesStore.tracearrDetailsIntegration,
            ) { state, tracearrRepo, tracearrIntegrationEnabled ->
                Triple(state, tracearrRepo, tracearrIntegrationEnabled)
            }.collectLatest { (state, tracearrRepo, tracearrIntegrationEnabled) ->
                if (tracearrRepo == null || !tracearrIntegrationEnabled) {
                    _tracearrState.value = TracearrMediaUiState(isTracearrConfigured = false)
                    return@collectLatest
                }

                val success = state as? UnifiedMediaDetailsUiState.Success
                val targetItem = success?.arrMedia

                val resolvedTmdbId =
                    initialTmdbId ?: when (targetItem) {
                        is ArrMovie -> targetItem.tmdbId.takeIf { it > 0 }
                        is ArrSeries -> targetItem.tmdbId?.takeIf { it > 0 }
                        else -> null
                    }

                val resolvedReqType =
                    initialRequestType ?: when (targetItem) {
                        is ArrMovie -> RequestType.Movie
                        is ArrSeries -> RequestType.Tv
                        else -> null
                    }

                val isMovieOrTv = resolvedReqType == RequestType.Movie || resolvedReqType == RequestType.Tv

                if (resolvedTmdbId != null && resolvedTmdbId > 0 && isMovieOrTv) {
                    val ref =
                        if (resolvedReqType == RequestType.Tv) {
                            "show:tmdb:$resolvedTmdbId"
                        } else {
                            "movie:tmdb:$resolvedTmdbId"
                        }
                    currentTracearrRef = ref

                    _tracearrState.update { it.copy(isTracearrConfigured = true, isLoading = true) }

                    val statsResult = tracearrRepo.getMediaStats(ref)
                    val watchersResult = tracearrRepo.getMediaWatchers(ref)
                    val historyResult = tracearrRepo.getMediaHistory(ref, cursor = null, pageSize = 25)

                    _tracearrState.update { currentState ->
                        currentState.copy(
                            isTracearrConfigured = true,
                            stats = (statsResult as? NetworkResult.Success)?.data,
                            watchers = (watchersResult as? NetworkResult.Success)?.data,
                            historyItems = (historyResult as? NetworkResult.Success)?.data?.data ?: emptyList(),
                            nextHistoryCursor = (historyResult as? NetworkResult.Success)?.data?.meta?.nextCursor,
                            isLoading = false,
                        )
                    }
                } else {
                    _tracearrState.value = TracearrMediaUiState(isTracearrConfigured = false)
                }
            }
        }
    }

    fun selectTracearrStatsWindow(window: TracearrStatsWindowType) {
        _tracearrState.update { it.copy(selectedStatsWindow = window) }
    }

    fun loadMoreTracearrHistory(
        scope: CoroutineScope,
        getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
        preferencesStore: PreferencesStore,
    ) {
        val currentState = _tracearrState.value
        val cursor = currentState.nextHistoryCursor ?: return
        if (currentState.isLoadingHistoryMore) return

        scope.launch {
            val enabled = preferencesStore.tracearrDetailsIntegration.firstOrNull() ?: false
            if (!enabled) return@launch
            val repo = getTracearrInstanceRepositoryUseCase.observeSelected().firstOrNull() ?: return@launch
            val ref = currentTracearrRef ?: return@launch

            _tracearrState.update { it.copy(isLoadingHistoryMore = true) }
            when (val res = repo.getMediaHistory(ref, cursor = cursor, pageSize = 25)) {
                is NetworkResult.Success -> {
                    _tracearrState.update { state ->
                        state.copy(
                            historyItems = state.historyItems + res.data.data,
                            nextHistoryCursor = res.data.meta?.nextCursor,
                            isLoadingHistoryMore = false,
                        )
                    }
                }
                else -> {
                    _tracearrState.update { it.copy(isLoadingHistoryMore = false) }
                }
            }
        }
    }
}
