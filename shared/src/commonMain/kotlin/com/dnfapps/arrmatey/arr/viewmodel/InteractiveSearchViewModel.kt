package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.CustomFormat
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.QualityInfo
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.api.model.ReleaseProtocol
import com.dnfapps.arrmatey.arr.api.model.SeriesRelease
import com.dnfapps.arrmatey.arr.state.DownloadState
import com.dnfapps.arrmatey.arr.state.InteractiveSearchUiState
import com.dnfapps.arrmatey.arr.state.ReleaseLibrary
import com.dnfapps.arrmatey.arr.usecase.DownloadReleaseUseCase
import com.dnfapps.arrmatey.arr.usecase.GetReleasesUseCase
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.compose.utils.ReleaseSortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.extensions.orderedSortedWith
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InteractiveSearchViewModel(
    private val instanceType: InstanceType,
    defaultFilterBy: ReleaseFilterBy,
    private val getReleasesUseCase: GetReleasesUseCase,
    private val downloadReleaseUseCase: DownloadReleaseUseCase,
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase
): ViewModel() {

    private val _releaseUiState = MutableStateFlow<ReleaseLibrary>(ReleaseLibrary.Initial)
    val releaseUiState: StateFlow<ReleaseLibrary> = _releaseUiState.asStateFlow()

    private val _downloadReleaseState = MutableStateFlow<DownloadState>(DownloadState.Initial)
    val downloadReleaseState: StateFlow<DownloadState> = _downloadReleaseState.asStateFlow()

    private val _filterUiState = MutableStateFlow(InteractiveSearchUiState(filterBy = defaultFilterBy))
    val filterUiState: StateFlow<InteractiveSearchUiState> = _filterUiState.asStateFlow()

    private val _downloadStatus = MutableStateFlow<Boolean?>(null)
    val downloadStatus: StateFlow<Boolean?> = _downloadStatus.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        observeReleases()
        observeDownloadStatus()
    }

    private fun observeReleases() {
        viewModelScope.launch {
            combine(
                getReleasesUseCase(instanceType),
                _filterUiState,
                _searchQuery
            ) { release, filter, query ->
                when (release) {
                    is ReleaseLibrary.Success -> {
                        val sorted = applySorting(release.items, filter)
                        val filtered = applyFiltering(sorted, filter, query)
                        release.copy(items = filtered)
                    }
                    else -> release
                }
            }.collect { state ->
                _releaseUiState.value = state
            }
        }
    }

    private fun applySorting(
        items: List<ArrRelease>,
        filter: InteractiveSearchUiState
    ): List<ArrRelease> {
        val comparator: Comparator<ArrRelease> = when (filter.sortBy) {
            ReleaseSortBy.Weight -> compareBy { it.releaseWeight }
            ReleaseSortBy.Age -> compareBy { it.ageMinutes }
            ReleaseSortBy.Quality -> compareBy { it.quality?.qualityLabel }
            ReleaseSortBy.Seeders -> compareBy { it.seeders }
            ReleaseSortBy.FileSize -> compareBy { it.size }
            ReleaseSortBy.CustomScore -> compareBy { it.customFormatScore }
        }
        return items.orderedSortedWith(filter.sortOrder, comparator)
    }

    private inline fun <reified T :ArrRelease> applyFiltering(
        items: List<T>,
        filter: InteractiveSearchUiState,
        query: String
    ): List<T> {
        if (items.isEmpty()) return items

        val filtered = items.filter { item ->
            (filter.language == null || item.languages.any { it.id == filter.language.id }) &&
            (filter.protocol == null || item.protocol == filter.protocol) &&
            (filter.indexer == null || item.indexer == filter.indexer) &&
            (filter.quality == null || item.quality?.quality?.id == filter.quality.quality.id) &&
            (filter.customFormat == null || item.customFormats.any { it.id == filter.customFormat.id }) &&
            (query.isEmpty() || item.title.contains(query, ignoreCase = true))
        }
        return when (filtered.firstOrNull()) {
            null -> filtered
            is SeriesRelease -> {
                @Suppress("UNCHECKED_CAST")
                seriesFiltering(filtered as List<SeriesRelease>, filter) as List<T>
            }
            else -> filtered
        }
    }

    private fun seriesFiltering(
        items: List<SeriesRelease>,
        filter: InteractiveSearchUiState
    ): List<SeriesRelease> = when (filter.filterBy) {
        ReleaseFilterBy.Any -> items
        ReleaseFilterBy.SeasonPack -> items.filter { it.fullSeason }
        ReleaseFilterBy.SingleEpisode -> items.filter { !it.fullSeason }
    }

    private fun observeDownloadStatus() {
        viewModelScope.launch {
            getArrInstanceRepositoryUseCase.observeSelected(instanceType)
                .filterNotNull()
                .collectLatest { repository ->
                    repository.downloadStatus.collect { status ->
                        _downloadReleaseState.value = status
                        _downloadStatus.value = when (status) {
                            is DownloadState.Success -> true
                            is DownloadState.Error -> false
                            else -> null
                        }
                    }
                }
        }
    }

    fun getRelease(params: ReleaseParams) {
        viewModelScope.launch {
            getReleasesUseCase.fetch(instanceType, params)
        }
    }

    fun downloadRelease(release: ArrRelease, force: Boolean = false) {
        viewModelScope.launch {
            _downloadReleaseState.value = DownloadState.Loading(release.guid)
            downloadReleaseUseCase(instanceType, release, force)
        }
    }

    fun resetDownloadState() {
        _downloadReleaseState.value = DownloadState.Initial
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(sortBy: ReleaseSortBy) {
        _filterUiState.update {
            it.copy(sortBy = sortBy)
        }
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _filterUiState.update {
            it.copy(sortOrder = sortOrder)
        }
    }

    fun setFilterBy(filterBy: ReleaseFilterBy) {
        _filterUiState.update {
            it.copy(filterBy = filterBy)
        }
    }

    fun setFilterLanguage(language: Language?) {
        _filterUiState.update {
            it.copy(language = language)
        }
    }

    fun setFilterIndexer(indexer: String?) {
        _filterUiState.update {
            it.copy(indexer = indexer)
        }
    }

    fun setFilterQuality(qualityInfo: QualityInfo?) {
        _filterUiState.update {
            it.copy(quality = qualityInfo)
        }
    }

    fun setFilterProtocol(protocol: ReleaseProtocol?) {
        _filterUiState.update {
            it.copy(protocol = protocol)
        }
    }

    fun setFilterCustomFormat(customFormat: CustomFormat?) {
        _filterUiState.update {
            it.copy(customFormat = customFormat)
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            getReleasesUseCase.clear(instanceType)
        }
        super.onCleared()
    }
}