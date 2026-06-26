package com.dnfapps.arrmatey.bazarr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitle
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSubtitleLanguage
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.bazarr.state.BazarrSubtitlesUiState
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Backs the "Subtitles" section embedded in Sonarr (episode) and Radarr (movie) detail
 * screens. Resolves the item in the selected Bazarr instance by the shared Sonarr/Radarr
 * ids, exposing the downloaded and missing subtitles plus auto-search and delete actions.
 */
class BazarrMediaSubtitlesViewModel(
    private val target: BazarrMediaTarget,
    private val getBazarrInstanceRepositoryUseCase: GetBazarrInstanceRepositoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<BazarrSubtitlesUiState>(BazarrSubtitlesUiState.Loading)
    val state: StateFlow<BazarrSubtitlesUiState> = _state.asStateFlow()

    private val _operationState = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val operationState: StateFlow<OperationStatus> = _operationState.asStateFlow()

    init {
        load()
    }

    private suspend fun repo() =
        getBazarrInstanceRepositoryUseCase.observeSelected().firstOrNull()

    fun load() {
        viewModelScope.launch {
            _state.value = BazarrSubtitlesUiState.Loading
            val repo = repo()
            if (repo == null) {
                _state.value = BazarrSubtitlesUiState.NoInstance
                return@launch
            }
            when (target) {
                is BazarrMediaTarget.Episode -> {
                    repo.getEpisodes(target.seriesId)
                        .onSuccess { episodes ->
                            val episode = episodes.firstOrNull { it.sonarrEpisodeId == target.episodeId }
                            _state.value = if (episode == null) {
                                BazarrSubtitlesUiState.NotTracked
                            } else {
                                val grouped = episode.subtitles.groupBy { it.isEmbedded }
                                BazarrSubtitlesUiState.Success(
                                    present = grouped[false] ?: emptyList(),
                                    embedded = grouped[true] ?: emptyList(),
                                    missing = episode.missingSubtitles
                                )
                            }
                        }
                        .onError { _, message, _ ->
                            _state.value = BazarrSubtitlesUiState.Error(message ?: "Failed to load subtitles")
                        }
                }
                is BazarrMediaTarget.Movie -> {
                    repo.getMovie(target.radarrId)
                        .onSuccess { movie ->
                            _state.value = if (movie == null) {
                                BazarrSubtitlesUiState.NotTracked
                            } else {
                                val grouped = movie.subtitles.groupBy { it.isEmbedded }
                                BazarrSubtitlesUiState.Success(
                                    present = grouped[false] ?: emptyList(),
                                    embedded = grouped[true] ?: emptyList(),
                                    missing = movie.missingSubtitles
                                )
                            }
                        }
                        .onError { _, message, _ ->
                            _state.value = BazarrSubtitlesUiState.Error(message ?: "Failed to load subtitles")
                        }
                }
            }
        }
    }

    fun autoSearch(language: BazarrSubtitleLanguage) {
        viewModelScope.launch {
            val repo = repo() ?: return@launch
            _operationState.value = OperationStatus.InProgress
            val op = when (target) {
                is BazarrMediaTarget.Episode -> repo.autoSearchEpisodeSubtitles(
                    target.seriesId, target.episodeId, language.code2, language.forced, language.hi
                )
                is BazarrMediaTarget.Movie -> repo.autoSearchMovieSubtitles(
                    target.radarrId, language.code2, language.forced, language.hi
                )
            }
            op
                .onSuccess { _operationState.value = OperationStatus.Success(); load() }
                .onError { code, message, cause ->
                    _operationState.value = OperationStatus.Error(code, message, cause)
                }
        }
    }

    fun delete(subtitle: BazarrSubtitle) {
        val path = subtitle.path ?: return
        viewModelScope.launch {
            val repo = repo() ?: return@launch
            _operationState.value = OperationStatus.InProgress
            val op = when (target) {
                is BazarrMediaTarget.Episode -> repo.deleteEpisodeSubtitle(
                    target.seriesId, target.episodeId, subtitle.code2, subtitle.forced, subtitle.hi, path
                )
                is BazarrMediaTarget.Movie -> repo.deleteMovieSubtitle(
                    target.radarrId, subtitle.code2, subtitle.forced, subtitle.hi, path
                )
            }
            op
                .onSuccess { _operationState.value = OperationStatus.Success(); load() }
                .onError { code, message, cause ->
                    _operationState.value = OperationStatus.Error(code, message, cause)
                }
        }
    }

    fun clearOperation() {
        _operationState.value = OperationStatus.Idle
    }
}
