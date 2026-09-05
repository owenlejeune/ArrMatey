package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.BookshelfClient
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookEdition
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.api.model.BookSeries
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import dev.shivathapaa.logger.api.Logger
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ReadarrRepository(
    instance: Instance,
    httpClient: HttpClient,
    logger: Logger,
) : ArrInstanceRepository(instance, httpClient, logger) {
    val bookshelfClient: BookshelfClient = client as? BookshelfClient ?: BookshelfClient(instance, httpClient)

    private val _authorSeries = MutableStateFlow<Map<Long, List<BookSeries>>>(emptyMap())
    override val authorSeries: StateFlow<Map<Long, List<BookSeries>>> = _authorSeries.asStateFlow()

    private val _authorBookFiles = MutableStateFlow<Map<Long, List<BookFile>>>(emptyMap())
    override val authorBookFiles: StateFlow<Map<Long, List<BookFile>>> = _authorBookFiles.asStateFlow()

    private val _booksLibrary = MutableStateFlow<List<Book>>(emptyList())
    override val booksLibrary: StateFlow<List<Book>> = _booksLibrary.asStateFlow()

    override val authorBooks: Flow<Map<Long, List<Book>>> =
        booksLibrary
            .map { books ->
                books
                    .filter { it.authorId != null }
                    .groupBy { it.authorId!! }
            }

    override suspend fun refreshLibrary() {
        libraryRepository.refreshLibrary(
            onBookLibraryUpdate = {
                bookshelfClient.getBooks().onSuccess {
                    _booksLibrary.value = it
                }
            },
        )
    }

    override suspend fun getAuthorSeries(authorId: Long): NetworkResult<List<BookSeries>> =
        bookshelfClient
            .getAuthorSeries(authorId)
            .onSuccess { result ->
                val currentMap = _authorSeries.value.toMutableMap()
                currentMap[authorId] = result
                _authorSeries.value = currentMap
            }

    override suspend fun getAuthorBookFiles(authorId: Long): NetworkResult<List<BookFile>> =
        bookshelfClient
            .getAuthorBookFiles(authorId)
            .onSuccess { result ->
                val currentMap = _authorBookFiles.value.toMutableMap()
                currentMap[authorId] = result
                _authorBookFiles.value = currentMap
            }

    override suspend fun deleteBookFiles(bookFilesIds: List<Long>): NetworkResult<Unit> = bookshelfClient.deleteBookFiles(bookFilesIds)

    override suspend fun toggleBookMonitor(book: Book): NetworkResult<Book> {
        libraryRepository.setMonitorStatus(OperationStatus.InProgress)

        val bookId = book.id
        val updatedMonitored = !book.monitored

        return bookshelfClient
            .setBookMonitorStatus(listOf(bookId), updatedMonitored)
            .onSuccess { responses ->
                val response = responses.firstOrNull()
                val isMonitored = response?.monitored ?: updatedMonitored
                libraryRepository.setMonitorStatus(
                    OperationStatus.Success(
                        if (isMonitored) "Book monitored" else "Book unmonitored",
                    ),
                )
                val updatedBook = book.copy(monitored = isMonitored)
                updateBookInCache(updatedBook)
            }.onError { code, message, cause ->
                libraryRepository.setMonitorStatus(OperationStatus.Error(code, message, cause))
            }.map { responses ->
                val response = responses.firstOrNull()
                book.copy(monitored = response?.monitored ?: updatedMonitored)
            }.also {
                libraryRepository.setMonitorStatus(OperationStatus.Idle)
            }
    }

    private fun updateBookInCache(book: Book) {
        _booksLibrary.update { currentList ->
            currentList.map { if (it.id == book.id) book else it }
        }
    }

    override suspend fun getBookEditions(bookId: Long): NetworkResult<List<BookEdition>> = bookshelfClient.getBookEditions(bookId)
}
