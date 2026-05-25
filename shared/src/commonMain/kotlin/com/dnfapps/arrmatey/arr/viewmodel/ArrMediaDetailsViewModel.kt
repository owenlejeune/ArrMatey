package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.state.MediaDetailsUiState
import com.dnfapps.arrmatey.arr.usecase.DeleteAlbumFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteMediaUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteMovieFileUseCase
import com.dnfapps.arrmatey.arr.usecase.DeleteSeasonFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetMediaDetailsUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformAutomaticSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformRefreshUseCase
import com.dnfapps.arrmatey.arr.usecase.ToggleMonitorUseCase
import com.dnfapps.arrmatey.arr.usecase.UpdateMediaUseCase
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArrMediaDetailsViewModel(
    private val mediaId: Long,
    private val instanceType: InstanceType,
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
    private val getMediaDetailsUseCase: GetMediaDetailsUseCase,
    private val toggleMonitorUseCase: ToggleMonitorUseCase,
    private val performAutomaticSearchUseCase: PerformAutomaticSearchUseCase,
    private val updateMediaUseCase: UpdateMediaUseCase,
    private val deleteMediaUseCase: DeleteMediaUseCase,
    private val deleteMovieFileUseCase: DeleteMovieFileUseCase,
    private val deleteSeasonFilesUseCase: DeleteSeasonFilesUseCase,
    private val deleteAlbumFilesUseCase: DeleteAlbumFilesUseCase,
    private val performRefreshUseCase: PerformRefreshUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<MediaDetailsUiState>(MediaDetailsUiState.Initial)
    val uiState: StateFlow<MediaDetailsUiState> = _uiState.asStateFlow()

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private val _monitorStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val monitorStatus: StateFlow<OperationStatus> = _monitorStatus.asStateFlow()

    private val _editItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val editItemStatus: StateFlow<OperationStatus> = _editItemStatus.asStateFlow()

    private val _isMonitored = MutableStateFlow(false)
    val isMonitored: StateFlow<Boolean> = _isMonitored.asStateFlow()

    private val _automaticSearchIds = MutableStateFlow<Set<Long>>(emptySet())
    val automaticSearchIds: StateFlow<Set<Long>> = _automaticSearchIds.asStateFlow()

    private val _lastSearchResult = MutableStateFlow<Boolean?>(null)
    val lastSearchResult: StateFlow<Boolean?> = _lastSearchResult.asStateFlow()

    private val _deleteStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteStatus: StateFlow<OperationStatus> = _deleteStatus.asStateFlow()

    private val _deleteSeasonStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteSeasonStatus: StateFlow<OperationStatus> = _deleteSeasonStatus.asStateFlow()

    private val _deleteAlbumStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteAlbumStatus: StateFlow<OperationStatus> = _deleteAlbumStatus.asStateFlow()

    private val _deleteMovieFileStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteMovieFileStatus: StateFlow<OperationStatus> = _deleteMovieFileStatus.asStateFlow()


    private val _qualityProfiles = MutableStateFlow<List<QualityProfile>>(emptyList())
    val qualityProfiles: StateFlow<List<QualityProfile>> = _qualityProfiles.asStateFlow()

    private val _rootFolders = MutableStateFlow<List<RootFolder>>(emptyList())
    val rootFolders: StateFlow<List<RootFolder>> = _rootFolders.asStateFlow()

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    private var currentRepository: ArrInstanceRepository? = null

    init {
        observeSelectedInstance()
    }

    private fun observeSelectedInstance() {
        viewModelScope.launch {
            getArrInstanceRepositoryUseCase.observeSelected(instanceType)
                .filterNotNull()
                .collectLatest { repository ->
                    currentRepository = repository
                    loadData(repository)
                }
        }
    }

    private fun loadData(repository: ArrInstanceRepository) {
        viewModelScope.launch {
            getMediaDetailsUseCase(mediaId, repository.instance.id)
                .collect { state ->
                    _uiState.value = state
                    if (state is MediaDetailsUiState.Success) {
                        _isMonitored.value = state.item.monitored
                    }
                }
        }

        viewModelScope.launch {
            repository.getItemHistory(mediaId)
                .onSuccess { _history.value = it }
        }
        viewModelScope.launch {
            repository.monitorStatus.collect { status ->
                _monitorStatus.value = status
            }
        }
        viewModelScope.launch {
            repository.qualityProfiles.collect { profiles ->
                _qualityProfiles.value = profiles
            }
        }
        viewModelScope.launch {
            repository.rootFolders.collect { folders ->
                _rootFolders.value = folders
            }
        }
        viewModelScope.launch {
            repository.tags.collect { tags ->
                _tags.value = tags
            }
        }
        viewModelScope.launch {
            repository.editItemStatus.collect { status ->
                _editItemStatus.value = status
            }
        }

        viewModelScope.launch {
            repository.artistAlbums.collect { albumMap ->
                val artistAlbums = albumMap[mediaId] ?: return@collect

                _uiState.update { currentState ->
                    if (currentState is MediaDetailsUiState.Success) {
                        currentState.copy(albums = artistAlbums)
                    } else {
                        currentState
                    }
                }
            }
        }
    }

    fun refreshDetails() {
        currentRepository?.let {
            loadData(it)
        }
    }

    fun updateItem(item: ArrMedia) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            updateMediaUseCase(item, repository)
        }
    }

    fun editItem(item: ArrMedia, moveFiles: Boolean = false) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            updateMediaUseCase.edit(item, moveFiles, repository)
        }
    }

    fun toggleMonitored() {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            val item = (uiState.value as? MediaDetailsUiState.Success)?.item ?: return@launch
            toggleMonitorUseCase.toggleMedia(item, repository)
        }
    }

    fun toggleSeasonMonitored(seasonNumber: Int) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            toggleMonitorUseCase.toggleSeason(mediaId, seasonNumber, repository)
        }
    }

    fun toggleEpisodeMonitored(episode: Episode) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            toggleMonitorUseCase.toggleEpisode(episode, repository)
        }
    }

    fun toggleAlbumMonitored(album: ArrAlbum) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            toggleMonitorUseCase.toggleAlbum(album, repository)
        }
    }

    fun toggleBookMonitored(book: Book) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            toggleMonitorUseCase.toggleBook(book, repository)
        }
    }

    fun toggleBookSeriesMonitored(books: List<Book>) {
        books.forEach { book -> toggleBookMonitored(book) }
    }

    fun deleteMedia(deleteFiles: Boolean, addImportExclusion: Boolean) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            deleteMediaUseCase(mediaId, deleteFiles, addImportExclusion, repository)
                .collect { status ->
                    _deleteStatus.value = status
                }
        }
    }

    fun deleteSeasonFiles(seasonNumber: Int) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            deleteSeasonFilesUseCase(mediaId, seasonNumber, repository)
                .collect { status ->
                    _deleteSeasonStatus.value = status
                }
        }
    }

    fun deleteAlbumFiles(albumId: Long) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            deleteAlbumFilesUseCase(mediaId, albumId, repository)
                .collect { status ->
                    _deleteAlbumStatus.value = status
                }
        }
    }

    fun deleteMovieFile() {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            deleteMovieFileUseCase(mediaId, repository)
                .collect { status ->
                    _deleteMovieFileStatus.value = status
                }
        }
    }

    fun performRefresh() {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            performRefreshUseCase(mediaId, instanceType, repository)
        }
    }

    fun performAutomaticLookup() {
        runSearch(mediaId)
    }

    fun performEpisodeAutomaticLookup(episodeId: Long) {
        runSearch(episodeId, episodeId)
    }

    fun performSeasonAutomaticLookup(seasonNumber: Int) {
        runSearch(mediaId, seasonNumber = seasonNumber)
    }

    fun performAlbumAutomaticLookup(albumId: Long) {
        runSearch(albumId, albumId)
    }

    fun performBookAutomaticLookup(bookId: Long) {
        runSearch(bookId, bookId)
    }

    private fun runSearch(
        trackingId: Long,
        episodeId: Long? = null,
        seasonNumber: Int? = null,
        albumId: Long? = null
    ) {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            updateSearchIds(trackingId, add = true)

            performAutomaticSearchUseCase(mediaId, instanceType, repository, episodeId, seasonNumber, albumId)
                .onSuccess { _lastSearchResult.value = true }
                .onError { _, _, _ -> _lastSearchResult.value = false }

            updateSearchIds(trackingId, add = false)
            _lastSearchResult.value = null
        }
    }

    private fun updateSearchIds(id: Long, add: Boolean) {
        _automaticSearchIds.update { current ->
            if (add) current + id else current - id
        }
    }
}