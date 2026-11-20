package com.dnfapps.arrmatey.newest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.ProwlarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class NewestDownloadableViewModel(
    private val instanceManager: InstanceManager,
    private val getNewestDownloadableUseCase: GetNewestDownloadableUseCase,
) : ViewModel() {
    private val _moviesState = MutableStateFlow<NewestDownloadableState>(NewestDownloadableState.Initial)
    val moviesState: StateFlow<NewestDownloadableState> = _moviesState.asStateFlow()

    private val _tvState = MutableStateFlow<NewestDownloadableState>(NewestDownloadableState.Initial)
    val tvState: StateFlow<NewestDownloadableState> = _tvState.asStateFlow()

    private val _hasProwlarr = MutableStateFlow(false)
    val hasProwlarr: StateFlow<Boolean> = _hasProwlarr.asStateFlow()

    private val _hasSeerr = MutableStateFlow(false)
    val hasSeerr: StateFlow<Boolean> = _hasSeerr.asStateFlow()

    private val jobs = mutableMapOf<ReleaseKind, Job>()
    private var prowlarr: ProwlarrInstanceRepository? = null
    private var seerr: SeerrInstanceRepository? = null

    init {
        viewModelScope.launch {
            combine(
                instanceManager.getSelectedProwlarrRepository(),
                instanceManager.getSelectedSeerrRepository(),
            ) { prowlarrRepo, seerrRepo -> prowlarrRepo to seerrRepo }
                .distinctUntilChanged()
                .collectLatest { (prowlarrRepo, seerrRepo) ->
                    prowlarr = prowlarrRepo
                    seerr = seerrRepo
                    _hasProwlarr.value = prowlarrRepo != null
                    _hasSeerr.value = seerrRepo != null
                    load()
                }
        }
    }

    /**
     * Loads the lists if nothing has loaded yet, or the last load failed.
     * Screens call this when they open, so a screen created before the
     * instances were known still fetches on its own.
     */
    fun ensureLoaded() {
        val idle = { state: NewestDownloadableState ->
            state is NewestDownloadableState.Initial || state is NewestDownloadableState.Error
        }
        if (prowlarr != null && (idle(_moviesState.value) || idle(_tvState.value))) load()
    }

    fun refresh() {
        viewModelScope.launch { load() }
    }

    private fun load() {
        val prowlarrRepo = prowlarr
        if (prowlarrRepo == null) {
            jobs.values.forEach { it.cancel() }
            jobs.clear()
            _moviesState.value = NewestDownloadableState.Initial
            _tvState.value = NewestDownloadableState.Initial
            return
        }
        val seerrRepo = seerr
        // Movies and TV are separate Prowlarr searches and run side by side.
        // A new load replaces the one in flight instead of running a second slow search.
        ReleaseKind.entries.forEach { kind ->
            jobs[kind]?.cancel()
            jobs[kind] = viewModelScope.launch { fetch(prowlarrRepo, seerrRepo, kind) }
        }
    }

    private suspend fun fetch(
        prowlarrRepo: ProwlarrInstanceRepository,
        seerrRepo: SeerrInstanceRepository?,
        kind: ReleaseKind,
    ) {
        if (stateFor(kind) !is NewestDownloadableState.Success) {
            setState(kind, NewestDownloadableState.Loading)
        }
        getNewestDownloadableUseCase(prowlarrRepo, seerrRepo, kind).collect { result ->
            when (result) {
                is NetworkResult.Success -> setState(kind, NewestDownloadableState.Success(result.data))
                is NetworkResult.Error -> setState(kind, NewestDownloadableState.Error(result.message ?: "Failed to load newest releases"))
                is NetworkResult.Loading -> Unit
            }
        }
    }

    private fun stateFor(kind: ReleaseKind): NewestDownloadableState = when (kind) {
        ReleaseKind.Movie -> _moviesState.value
        ReleaseKind.Tv -> _tvState.value
    }

    private fun setState(kind: ReleaseKind, state: NewestDownloadableState) {
        when (kind) {
            ReleaseKind.Movie -> _moviesState.value = state
            ReleaseKind.Tv -> _tvState.value = state
        }
    }
}
