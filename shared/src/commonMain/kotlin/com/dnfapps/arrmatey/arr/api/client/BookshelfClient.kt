package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ApplyTags
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.AuthorBulkEditBody
import com.dnfapps.arrmatey.arr.api.model.AuthorEditorBody
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitorOptions
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitorType
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitoringBody
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitoringOption
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookEdition
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.api.model.BookFileBulkDeleteBody
import com.dnfapps.arrmatey.arr.api.model.BookMonitorBody
import com.dnfapps.arrmatey.arr.api.model.BookSeries
import com.dnfapps.arrmatey.arr.api.model.BookshelfHistoryItem
import com.dnfapps.arrmatey.arr.api.model.BookshelfHistoryResponse
import com.dnfapps.arrmatey.arr.api.model.BookshelfRelease
import com.dnfapps.arrmatey.arr.api.model.ChaptarrReleaseResponse
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.CommandResponse
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.IdWrapper
import com.dnfapps.arrmatey.arr.api.model.MonitoredResponse
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate

class BookshelfClient(
    override val instance: Instance,
    httpClient: HttpClient,
) : BaseArrClient(httpClient),
    ArrClient {
    override suspend fun getLibrary(): NetworkResult<List<Author>> = get("author")

    override suspend fun getDetail(id: Long): NetworkResult<Author> = get("author/$id")

    override suspend fun update(item: ArrMedia): NetworkResult<Author> = put<ArrMedia, Author>("author/${item.id}", item)

    override suspend fun edit(
        item: ArrMedia,
        moveFiles: Boolean,
    ): NetworkResult<Unit> {
        val author =
            item as? Author
                ?: return NetworkResult.Error(message = "Item must be an Author instance")
        val id =
            author.id
                ?: return NetworkResult.Error(message = "Item id cannot be null")
        val body =
            AuthorEditorBody(
                authorIds = listOf(id),
                monitored = author.monitored,
                monitorNewItems = author.monitorNewItems,
                qualityProfileId = author.qualityProfileId,
                rootFolderPath = author.rootFolderPath,
                tags = author.tags,
                applyTags = ApplyTags.Replace,
                moveFiles = moveFiles,
            )
        return put("author/editor", body)
    }

    override suspend fun delete(
        id: Long,
        deleteFiles: Boolean,
        addImportExclusion: Boolean,
    ): NetworkResult<Unit> = delete(
        endpoint = "author/$id",
        params =
        mapOf(
            "deleteFiles" to deleteFiles,
            "addImportExclusion" to addImportExclusion,
        ),
    )

    override suspend fun setMonitorStatus(
        id: Long,
        monitorStatus: Boolean,
    ): NetworkResult<List<MonitoredResponse>> = put(
        "author/editor",
        mapOf(
            "monitored" to monitorStatus,
            "authorIds" to listOf(id),
        ),
    )

    override suspend fun lookup(params: LookupParams): NetworkResult<List<Author>> = get("author/lookup", mapOf("term" to params.query))

    override suspend fun addItemToLibrary(item: ArrMedia): NetworkResult<Author> = post<ArrMedia, Author>("author", item)

    override suspend fun performAutomaticSearch(id: Long): NetworkResult<CommandResponse> = post("command", CommandPayload.Author(id))

    override suspend fun getReleases(params: ReleaseParams): NetworkResult<List<BookshelfRelease>> {
        if (params !is ReleaseParams.Book) {
            return NetworkResult.Error(message = "Non-bookshelf params type: $params")
        }
        val queryParams = mapOf("bookId" to params.mediaId)
        return if (instance.type == InstanceType.Chaptarr) {
            get<ChaptarrReleaseResponse>("release", queryParams).map { response ->
                response.releases
            }
        } else {
            get("release", queryParams)
        }
    }

    override suspend fun getItemHistory(
        id: Long,
        page: Int,
        pageSize: Int,
        altId: Long?,
    ): NetworkResult<List<BookshelfHistoryItem>> = get<List<BookshelfHistoryItem>>(
        "history/author",
        buildMap {
            put("authorId", id)
            altId?.let { put("bookId", it) }
        },
    ).map { list ->
        list.map { it.copy(instanceId = instance.id, instanceName = instance.label, instanceType = instance.type) }
    }

    override suspend fun getHistory(
        page: Int,
        pageSize: Int,
    ): NetworkResult<List<HistoryItem>> = get<BookshelfHistoryResponse>(
        "history",
        mapOf<String, Any>(
            "page" to page,
            "pageSize" to pageSize,
        ),
    ).map { response ->
        response.records.map {
            it.copy(
                instanceId = instance.id,
                instanceName = instance.label,
                instanceType = instance.type,
            )
        }
    }

    suspend fun getAuthorSeries(id: Long): NetworkResult<List<BookSeries>> = get("series", mapOf("authorId" to id))

    suspend fun getAuthorBookFiles(id: Long): NetworkResult<List<BookFile>> = get("bookFile", mapOf("authorId" to id))

    suspend fun getBookFiles(bookId: Long): NetworkResult<List<BookFile>> = get("bookFile", mapOf("bookId" to bookId))

    suspend fun getBooks(authorId: Long? = null): NetworkResult<List<Book>> = get("book", buildMap { authorId?.let { put("authorId", it) } })

    suspend fun updateBook(book: Book): NetworkResult<Book> = put("book/${book.id}", book)

    suspend fun setBookMonitorStatus(
        bookIds: List<Long>,
        monitored: Boolean,
    ): NetworkResult<List<MonitoredResponse>> = put<BookMonitorBody, List<MonitoredResponse>>("book/monitor", BookMonitorBody(bookIds, monitored))

    suspend fun getBookEditions(bookId: Long): NetworkResult<List<BookEdition>> = get("edition", mapOf("bookId" to bookId))

    suspend fun deleteBookFiles(bookFilesIds: List<Long>): NetworkResult<Unit> = delete("bookFiles/bulk", body = BookFileBulkDeleteBody(bookFilesIds))

    override suspend fun getCalendar(
        start: LocalDate,
        end: LocalDate,
    ): NetworkResult<List<Book>> = get<List<Book>>(
        "calendar",
        mapOf(
            "start" to start.toString(),
            "end" to end.toString(),
            "unmonitored" to true,
            "includeAuthor" to true,
            "includeBookFile" to true,
        ),
    ).map { it.map { bk -> bk.copy(instanceId = instance.id) } }

    override suspend fun updateMonitoring(
        ids: List<Long>,
        monitor: Any,
    ): NetworkResult<Unit> = when (monitor) {
        is AuthorMonitorType -> {
            post(
                endpoint = "bookshelf",
                body =
                AuthorMonitoringBody(
                    authors = ids.map { IdWrapper(it) },
                    monitoringOptions = AuthorMonitoringOption(monitor),
                ),
            )
        }

        is AuthorMonitorOptions -> {
            val body =
                AuthorBulkEditBody(
                    authorIds = ids,
                    monitored = monitor.monitored,
                    monitorNewItems = monitor.monitorNewItems,
                )
            put("author/editor", body)
        }

        else -> NetworkResult.Error(message = "Invalid monitor options")
    }
}
