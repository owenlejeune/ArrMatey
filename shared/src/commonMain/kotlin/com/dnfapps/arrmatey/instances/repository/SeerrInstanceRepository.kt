package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.client.paging.BasePagingSource
import com.dnfapps.arrmatey.client.paging.PageResult
import com.dnfapps.arrmatey.client.paging.PagingSource
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.seerr.api.client.SeerrClient
import com.dnfapps.arrmatey.seerr.api.client.SeerrClientImpl
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.CombinedRatings
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.Issue
import com.dnfapps.arrmatey.seerr.api.model.IssueBody
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.PersonCredits
import com.dnfapps.arrmatey.seerr.api.model.PersonDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaBody
import com.dnfapps.arrmatey.seerr.api.model.RequestResponse
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.RottenTomatoesRating
import com.dnfapps.arrmatey.seerr.api.model.Season
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.Service
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.arrmatey.seerr.service.MediaIssuePackageService
import com.dnfapps.arrmatey.seerr.service.MediaRequestPackageService
import com.dnfapps.arrmatey.seerr.state.RequestOperationsState
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.OperationStatus
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class SeerrInstanceRepository(
    override val instance: Instance,
    httpClient: HttpClient
): InstanceScopedRepository {
    val client: SeerrClient = SeerrClientImpl(instance, httpClient)
    private val mediaPackageService = MediaRequestPackageService(client)
    private val issuePackageService = MediaIssuePackageService(client)

    private val _loggedInUser = MutableStateFlow<SeerrUser?>(null)
    val loggedInUser: StateFlow<SeerrUser?> = _loggedInUser.asStateFlow()

    private val _operationsState = MutableStateFlow(RequestOperationsState())
    val operationsState: StateFlow<RequestOperationsState> = _operationsState.asStateFlow()

    private val _mediaDetailsCache = MutableStateFlow<Map<Long, RequestMediaDetails>>(emptyMap())

    private val _radarrServices = MutableStateFlow<List<Service>>(emptyList())
    val radarrServices: StateFlow<List<Service>> = _radarrServices.asStateFlow()

    private val _sonarrServices = MutableStateFlow<List<Service>>(emptyList())
    val sonarrServices: StateFlow<List<Service>> = _sonarrServices.asStateFlow()

    private val _users = MutableStateFlow<List<SeerrUser>>(emptyList())
    val users: StateFlow<List<SeerrUser>> = _users.asStateFlow()

    private val _pendingRequestsCount = MutableStateFlow(0)
    val pendingRequestsCount: StateFlow<Int> = _pendingRequestsCount.asStateFlow()

    private val _openIssuesCount = MutableStateFlow(0)
    val openIssuesCount: StateFlow<Int> = _openIssuesCount.asStateFlow()

    override suspend fun testConnection(): NetworkResult<Unit> =
        client.testConnection()

    suspend fun getLoggedInUser() {
        client.getUserInfo()
            .onSuccess { _loggedInUser.value = it }
    }

    suspend fun getUsers() {
        client.getUsers()
            .onSuccess { _users.value = it.results }
    }

    suspend fun refreshCounts() {
        client.getRequests(page = 1, pageSize = 1).onSuccess {
            _pendingRequestsCount.value = it.pageInfo.results
        }
        client.getIssues(page = 1, pageSize = 1).onSuccess {
            _openIssuesCount.value = it.pageInfo.results
        }
    }

    fun getRequestsPaging(): PagingSource<MediaRequestPackage> {
        return BasePagingSource(
            fetcher = { page ->
                client.getRequests(page = page)
            },
            processor = { response ->
                val enrichedRequests = mediaPackageService.enrichRequests(response.results)
                PageResult(
                    items = enrichedRequests,
                    totalItemCount = response.pageInfo.results,
                    hasNextPage = response.pageInfo.page < response.pageInfo.pages
                )
            }
        )
    }

    fun getTrendingPaging(): PagingSource<DiscoverResult> {
        return BasePagingSource(
            fetcher = { page ->
                client.getTrending(page = page)
            },
            processor = { response ->
                PageResult(
                    items = response.results,
                    totalItemCount = response.totalResults,
                    hasNextPage = response.page < response.totalPages
                )
            }
        )
    }

    fun getDiscoverMoviesPaging(): PagingSource<DiscoverResult> {
        return BasePagingSource(
            fetcher = { page ->
                client.getDiscoverMovies(page = page)
            },
            processor = { response ->
                PageResult(
                    items = response.results,
                    totalItemCount = response.totalResults,
                    hasNextPage = response.page < response.totalPages
                )
            }
        )
    }

    fun getDiscoverTvPaging(): PagingSource<DiscoverResult> {
        return BasePagingSource(
            fetcher = { page ->
                client.getDiscoverTv(page = page)
            },
            processor = { response ->
                PageResult(
                    items = response.results,
                    totalItemCount = response.totalResults,
                    hasNextPage = response.page < response.totalPages
                )
            }
        )
    }

    fun getUpcomingMoviesPaging(): PagingSource<DiscoverResult> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        return BasePagingSource(
            fetcher = { page ->
                client.getUpcomingMovies(page = page, today = today)
            },
            processor = { response ->
                PageResult(
                    items = response.results,
                    totalItemCount = response.totalResults,
                    hasNextPage = response.page < response.totalPages
                )
            }
        )
    }

    fun getUpcomingTvPaging(): PagingSource<DiscoverResult> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        return BasePagingSource(
            fetcher = { page ->
                client.getUpcomingTv(page = page, today = today)
            },
            processor = { response ->
                PageResult(
                    items = response.results,
                    totalItemCount = response.totalResults,
                    hasNextPage = response.page < response.totalPages
                )
            }
        )
    }

    fun searchPaging(query: String): PagingSource<DiscoverResult> {
        return BasePagingSource(
            fetcher = { page ->
                client.search(query = query, page = page)
            },
            processor = { response ->
                PageResult(
                    items = response.results,
                    totalItemCount = response.totalResults,
                    hasNextPage = response.page < response.totalPages
                )
            }
        )
    }

    suspend fun getRequests(
        page: Int = 1,
        pageSize: Int = 10
    ): NetworkResult<RequestResponse> {
        return client.getRequests(page = page, pageSize = pageSize)
    }

    suspend fun createRequest(request: RequestMediaBody): NetworkResult<MediaRequest> {
        return client.createRequest(request)
    }

    suspend fun setRequestStatus(
        requestId: Long,
        status: ApprovalStatus,
        profileId: Long? = null,
        rootFolder: String? = null,
        languageProfileId: Long? = null,
        seasons: List<Int>? = null
    ): NetworkResult<MediaRequest> {
        updateOperationsState(requestId, status, OperationStatus.InProgress)
        return client.setRequestStatus(requestId, status, profileId, rootFolder, languageProfileId, seasons)
            .onSuccess {
                updateOperationsState(requestId, status, OperationStatus.Success())
            }
            .onError { code, message, cause ->
                updateOperationsState(requestId, status, OperationStatus.Error(code, message, cause))
            }
    }

    suspend fun deleteRequest(requestId: Long): NetworkResult<Unit> {
        updateOperationsState(requestId, ApprovalStatus.Decline, OperationStatus.InProgress)
        return client.deleteRequest(requestId)
            .onSuccess { updateOperationsState(requestId, ApprovalStatus.Decline, OperationStatus.Success()) }
            .onError { code, message, cause ->
                updateOperationsState(requestId, ApprovalStatus.Decline, OperationStatus.Error(code, message, cause))
            }
    }

    suspend fun deleteMediaFile(requestId: Long, mediaId: Long, is4k: Boolean): NetworkResult<Unit> {
        updateOperationsState(requestId, ApprovalStatus.Decline, OperationStatus.InProgress)
        return client.deleteMediaFile(mediaId, is4k)
            .onSuccess { updateOperationsState(requestId, ApprovalStatus.Decline, OperationStatus.Success()) }
            .onError { code, message, cause ->
                updateOperationsState(requestId, ApprovalStatus.Decline, OperationStatus.Error(code, message, cause))
            }
    }

    suspend fun deleteMediaFile(mediaId: Long, is4k: Boolean): NetworkResult<Unit> {
        return client.deleteMediaFile(mediaId, is4k)
    }

    suspend fun clearMediaData(mediaId: Long): NetworkResult<Unit> {
        return client.clearMediaData(mediaId)
    }

    suspend fun markMediaAsAvailable(mediaId: Long, is4k: Boolean = false): NetworkResult<Unit> {
        return client.markMediaAsAvailable(mediaId, is4k)
    }

    private fun updateOperationsState(requestId: Long, status: ApprovalStatus, state: OperationStatus) {
        _operationsState.update {
            val currentStates = when (status) {
                ApprovalStatus.Approve -> it.approvalStates
                ApprovalStatus.Decline -> it.cancelStates
            }.toMutableMap()
            currentStates[requestId] = state
            it.copy(
                approvalStates = if (status == ApprovalStatus.Approve) currentStates else it.approvalStates,
                cancelStates = if (status == ApprovalStatus.Decline) currentStates else it.cancelStates
            )
        }
    }

    fun observeMediaDetails(
        tmdbId: Long,
        mediaType: RequestType
    ): Flow<NetworkResult<RequestMediaDetails>> = flow {
        emit(NetworkResult.Loading)

        _mediaDetailsCache.value[tmdbId]?.let {
            emit(NetworkResult.Success(it))
        }

        val result = when (mediaType) {
            RequestType.Movie -> client.getMovieDetails(tmdbId)
            RequestType.Tv -> client.getTvDetails(tmdbId)
            RequestType.Person -> client.getPersonDetails(tmdbId)
        }
        when (result) {
            is NetworkResult.Success<*> -> {
                val currentCache = _mediaDetailsCache.value.toMutableMap()
                currentCache[tmdbId] = (result as NetworkResult.Success<RequestMediaDetails>).data
                _mediaDetailsCache.value = currentCache
            }

            is NetworkResult.Error -> {
                emit(result)
                return@flow
            }

            is NetworkResult.Loading -> {}
        }

        _mediaDetailsCache
            .map { cache ->
                cache[tmdbId]?.let { NetworkResult.Success(it) }
                    ?: NetworkResult.Error(message = "Media not found in cache")
            }
            .collect { emit(it) }
    }

    suspend fun getTvRatings(tmdbId: Long): NetworkResult<RottenTomatoesRating> {
        return client.getTvRatings(tmdbId)
    }

    suspend fun getMovieRatings(tmdbId: Long): NetworkResult<CombinedRatings> {
        return client.getMovieRatings(tmdbId)
    }

    suspend fun getSeasonDetails(tmdbId: Long, seasonNumber: Int): NetworkResult<Season> {
        return client.getSeasonDetails(tmdbId, seasonNumber)
    }

    suspend fun submitIssue(issue: IssueBody): NetworkResult<Issue> {
        return client.submitIssue(issue)
    }

    fun getIssuesPaging(): PagingSource<MediaIssuePackage> {
        return BasePagingSource(
            fetcher = { page ->
                client.getIssues(page = page)
            },
            processor = { response ->
                val enrichedIssues = issuePackageService.enrichIssues(response.results)
                PageResult(
                    items = enrichedIssues,
                    totalItemCount = response.pageInfo.results,
                    hasNextPage = response.pageInfo.page < response.pageInfo.pages
                )
            }
        )
    }

    suspend fun submitIssueComment(issueId: Long, comment: String): NetworkResult<Issue> {
        return client.submitIssueComment(issueId, comment)
    }

    suspend fun getIssueDetails(issueId: Long): NetworkResult<Issue> {
        return client.getIssueDetails(issueId)
    }

    suspend fun getRadarrServices(): NetworkResult<List<Service>> {
        return client.getRadarrServices().onSuccess { _radarrServices.value = it }
    }

    suspend fun getSonarrServices(): NetworkResult<List<Service>> {
        return client.getSonarrServices().onSuccess { _sonarrServices.value = it }
    }

    suspend fun getRadarrDetails(serverId: Long): NetworkResult<ServiceDetails> {
        return client.getRadarrDetails(serverId)
    }

    suspend fun getSonarrDetails(serverId: Long): NetworkResult<ServiceDetails> {
        return client.getSonarrDetails(serverId)
    }

    suspend fun getPersonDetails(personId: Long): NetworkResult<PersonDetails> {
        return client.getPersonDetails(personId)
    }

    suspend fun getPersonCredits(personId: Long): NetworkResult<PersonCredits> {
        return client.getPersonCredits(personId)
    }

    suspend fun closeIssue(issueId: Long): NetworkResult<Unit> {
        return client.closeIssue(issueId)
    }
}