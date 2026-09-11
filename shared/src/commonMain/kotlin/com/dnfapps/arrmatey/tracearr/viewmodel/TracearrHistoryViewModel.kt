package com.dnfapps.arrmatey.tracearr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.repository.TracearrRepository
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.tracearr.state.TracearrHistoryState
import com.dnfapps.arrmatey.tracearr.usecase.GetTracearrHistoryUseCase
import com.dnfapps.arrmatey.tracearr.usecase.GetTracearrStreamsUseCase
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TracearrHistoryViewModel(
    getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
    private val getTracearrHistoryUseCase: GetTracearrHistoryUseCase,
    private val getTracearrStreamsUseCase: GetTracearrStreamsUseCase,
) : ViewModel() {

    val currentRepository: StateFlow<TracearrRepository?> =
        getTracearrInstanceRepositoryUseCase
            .observeSelected()
            .distinctUntilChanged { old, new -> old?.instance?.id == new?.instance?.id }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    private val _state = MutableStateFlow<TracearrHistoryState>(TracearrHistoryState.Initial)
    val state: StateFlow<TracearrHistoryState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedSession = MutableStateFlow<TracearrStreamSession?>(null)
    val selectedSession: StateFlow<TracearrStreamSession?> = _selectedSession.asStateFlow()

    private var nextCursor: String? = null
    private var isLoadingPage = false

    init {
        viewModelScope.launch {
            currentRepository.collect { repo ->
                if (repo == null) {
                    _state.value = TracearrHistoryState.NoInstance
                } else {
                    loadHistory(repo, isRefresh = true)
                }
            }
        }
    }

    fun refresh() {
        val repo = currentRepository.value ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            loadHistory(repo, isRefresh = true)
            _isRefreshing.value = false
        }
    }

    fun loadMore() {
        val repo = currentRepository.value ?: return
        val currentState = _state.value as? TracearrHistoryState.Success ?: return
        if (!currentState.hasMore || isLoadingPage || nextCursor == null) return

        viewModelScope.launch {
            isLoadingPage = true
            _state.update {
                if (it is TracearrHistoryState.Success) {
                    it.copy(isLoadingMore = true)
                } else it
            }

            getTracearrHistoryUseCase(repo, cursor = nextCursor, pageSize = 25)
                .onSuccess { response ->
                    val newItems = response.data
                    nextCursor = response.meta?.nextCursor
                    val hasMore = !nextCursor.isNullOrBlank()

                    _state.update { old ->
                        if (old is TracearrHistoryState.Success) {
                            old.copy(
                                items = old.items + newItems,
                                isLoadingMore = false,
                                hasMore = hasMore,
                                nextCursor = nextCursor,
                            )
                        } else old
                    }
                }
                .onError { _, _, _ ->
                    _state.update { old ->
                        if (old is TracearrHistoryState.Success) {
                            old.copy(isLoadingMore = false)
                        } else old
                    }
                }

            isLoadingPage = false
        }
    }

    private suspend fun loadHistory(repo: TracearrRepository, isRefresh: Boolean) {
        if (isRefresh) {
            _state.value = TracearrHistoryState.Loading
            nextCursor = null
        }

        isLoadingPage = true

        coroutineScope {
            val streamsDeferred = async { getTracearrStreamsUseCase(repo) }
            val historyDeferred = async { getTracearrHistoryUseCase(repo, cursor = null, pageSize = 25) }

            val streamsResult = streamsDeferred.await()
            val historyResult = historyDeferred.await()

            val activeStreams = (streamsResult as? NetworkResult.Success)?.data?.data ?: emptyList()

            historyResult
                .onSuccess { response ->
                    nextCursor = response.meta?.nextCursor
                    val hasMore = !nextCursor.isNullOrBlank()
                    _state.value = TracearrHistoryState.Success(
                        activeStreams = activeStreams,
                        items = response.data,
                        isLoadingMore = false,
                        hasMore = hasMore,
                        nextCursor = nextCursor,
                    )
                }
                .onError { _, msg, _ ->
                    _state.value = TracearrHistoryState.Error(msg ?: "Failed to load history")
                }
        }

        isLoadingPage = false
    }

    fun setSelectedStreamSession(session: TracearrStreamSession) {
        _selectedSession.value = session
    }

    fun setSelectedHistoryStream(historyItem: TracearrHistoryItem) {
        _selectedSession.value = historyItem.toStreamSession()
    }

    fun clearSelected() {
        _selectedSession.value = null
    }
}
