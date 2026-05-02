package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookEdition
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.state.HistoryState
import com.dnfapps.arrmatey.arr.usecase.DeleteBookFilesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetBookEditionUseCase
import com.dnfapps.arrmatey.arr.usecase.GetBookHistoryUseCase
import com.dnfapps.arrmatey.arr.usecase.PerformAutomaticSearchUseCase
import com.dnfapps.arrmatey.arr.usecase.ToggleMonitorUseCase
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class BookDetailsViewModel(
    private val authorId: Long,
    book: Book,
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
    private val toggleMonitorUseCase: ToggleMonitorUseCase,
    private val performAutomaticSearchUseCase: PerformAutomaticSearchUseCase,
    private val getBookHistoryUseCase: GetBookHistoryUseCase,
    private val deleteBookFilesUseCase: DeleteBookFilesUseCase,
    private val getBookEditionUseCase: GetBookEditionUseCase
): ViewModel() {

    private val _book = MutableStateFlow(book)
    val book: StateFlow<Book> = _book.asStateFlow()

    private val _bookFiles = MutableStateFlow<List<BookFile>>(emptyList())
    val bookFiles: StateFlow<List<BookFile>> = _bookFiles.asStateFlow()

    private val _bookEdition = MutableStateFlow<BookEdition?>(null)
    val bookEdition: StateFlow<BookEdition?> = _bookEdition.asStateFlow()

    private val _history = MutableStateFlow<HistoryState>(HistoryState.Initial)
    val history: StateFlow<HistoryState> = _history.asStateFlow()

    private val _monitorStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val monitorStatus: StateFlow<OperationStatus> = _monitorStatus.asStateFlow()

    private val _deleteStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val deleteStatus: StateFlow<OperationStatus> = _deleteStatus.asStateFlow()

    private var currentRepository: ArrInstanceRepository? = null

    init {
        observeSelectedInstance()
    }

    private fun observeSelectedInstance() {
        viewModelScope.launch {
            getArrInstanceRepositoryUseCase.observeSelected(InstanceType.Booksehelf)
                .filterNotNull()
                .collectLatest { repository ->
                    currentRepository = repository
                    observeData(repository)
                    refreshHistory()
                }
        }
    }

    private fun observeData(repository: ArrInstanceRepository) {
        viewModelScope.launch {
            repository.authorBooks
                .map { booksMap ->
                    booksMap[authorId]?.firstOrNull { it.id == _book.value.id }
                }
                .collect { book ->
                    book?.let { _book.value = it }
                }
        }
        viewModelScope.launch {
            repository.authorBookFiles
                .map { booksFilesMap ->
                    booksFilesMap[authorId]?.filter { it.bookId == _book.value.id }
                }
                .collect { bookFiles ->
                    _bookFiles.value = bookFiles ?: emptyList()
                }
        }

        viewModelScope.launch {
            repository.monitorStatus.collect { status ->
                _monitorStatus.value = status
            }
        }

        viewModelScope.launch {
            getBookEditionUseCase(_book.value.id, repository)
                .onSuccess { result ->
                    _bookEdition.value = result.firstOrNull { it.monitored }
                }
        }
    }

    fun toggleMonitor() {
        viewModelScope.launch {
            currentRepository?.let {
                toggleMonitorUseCase.toggleBook(_book.value, it)
            }
        }
    }

    fun executeAutomaticSearch() {
        viewModelScope.launch {
            currentRepository?.let {
                performAutomaticSearchUseCase(
                    mediaId = authorId,
                    type = InstanceType.Booksehelf,
                    repository = it,
                    bookId = _book.value.id
                )
            }
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            getBookHistoryUseCase(_book.value.id, authorId, repository)
                .collect { state ->
                    _history.value = state
                }
        }
    }

    fun deleteBook() {
        viewModelScope.launch {
            val repository = currentRepository ?: return@launch
            val bookFilesIds = _bookFiles.value.map { it.id }
            deleteBookFilesUseCase(bookFilesIds, repository)
                .collect { state ->
                    _deleteStatus.value = state
                    refreshHistory()
                }
        }
    }

    fun resetMonitorStatus() {
        _monitorStatus.value = OperationStatus.Idle
    }
}