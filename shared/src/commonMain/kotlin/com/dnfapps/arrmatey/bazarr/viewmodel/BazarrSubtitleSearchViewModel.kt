package com.dnfapps.arrmatey.bazarr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.bazarr.api.model.ProviderSubtitle
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.bazarr.state.SubtitleSearchState
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Manual subtitle search for a single episode or movie. Used both from a Wanted-list item
 * and from the subtitle section embedded in Sonarr/Radarr detail screens. Download status
 * is tracked per result so each row can show its own progress/outcome.
 */
class BazarrSubtitleSearchViewModel(
    private val target: BazarrMediaTarget,
    private val getBazarrInstanceRepositoryUseCase: GetBazarrInstanceRepositoryUseCase
) : ViewModel() {

    private val _searchState = MutableStateFlow<SubtitleSearchState>(SubtitleSearchState.Idle)
    val searchState: StateFlow<SubtitleSearchState> = _searchState.asStateFlow()

    private val _downloadStates = MutableStateFlow<Map<String, OperationStatus>>(emptyMap())
    val downloadStates: StateFlow<Map<String, OperationStatus>> = _downloadStates.asStateFlow()

    init {
        search()
    }

    private suspend fun repo(): BazarrInstanceRepository? =
        getBazarrInstanceRepositoryUseCase.observeSelected().firstOrNull()

    fun search() {
        viewModelScope.launch {
            _searchState.value = SubtitleSearchState.Loading
            val repo = repo()
            if (repo == null) {
                _searchState.value = SubtitleSearchState.Error("No Bazarr instance configured")
                return@launch
            }
            val result = when (target) {
                is BazarrMediaTarget.Episode -> repo.searchEpisodeSubtitles(target.episodeId)
                is BazarrMediaTarget.Movie -> repo.searchMovieSubtitles(target.radarrId)
            }
            result
                .onSuccess { _searchState.value = SubtitleSearchState.Success(it) }
                .onError { _, message, _ ->
                    _searchState.value = SubtitleSearchState.Error(message ?: "Search failed")
                }
        }
    }

    fun download(result: ProviderSubtitle) {
        viewModelScope.launch {
            val repo = repo() ?: return@launch
            val key = result.key()
            _downloadStates.update { it + (key to OperationStatus.InProgress) }
            val op = when (target) {
                is BazarrMediaTarget.Episode ->
                    repo.downloadEpisodeSubtitle(target.seriesId, target.episodeId, result)
                is BazarrMediaTarget.Movie ->
                    repo.downloadMovieSubtitle(target.radarrId, result)
            }
            op
                .onSuccess { _downloadStates.update { m -> m + (key to OperationStatus.Success()) } }
                .onError { code, message, cause ->
                    _downloadStates.update { m -> m + (key to OperationStatus.Error(code, message, cause)) }
                }
        }
    }

    /** Stable identity for a result row, used to key its download status. */
    fun ProviderSubtitle.key(): String = "$provider:$subtitle"
}
