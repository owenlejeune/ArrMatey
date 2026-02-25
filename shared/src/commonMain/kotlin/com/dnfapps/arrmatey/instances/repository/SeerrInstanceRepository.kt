package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.client.paging.BasePagingSource
import com.dnfapps.arrmatey.client.paging.PageResult
import com.dnfapps.arrmatey.client.paging.PagingSource
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.seerr.api.client.SeerrClient
import com.dnfapps.arrmatey.seerr.api.client.SeerrClientImpl
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestResponse
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.service.MediaRequestPackageService
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SeerrInstanceRepository(
    override val instance: Instance,
    httpClient: HttpClient
): InstanceScopedRepository {
    val client: SeerrClient = SeerrClientImpl(instance, httpClient)
    private val mediaPackageService = MediaRequestPackageService(client)

    private val _loggedInUser = MutableStateFlow<SeerrUser?>(null)
    val loggedInUser: StateFlow<SeerrUser?> = _loggedInUser.asStateFlow()

    override suspend fun testConnection(): NetworkResult<Unit> =
        client.testConnection()

    suspend fun getLoggedInUser() {
        client.getUserInfo()
            .onSuccess { _loggedInUser.value = it }
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
                    hasNextPage = response.pageInfo.page < response.pageInfo.pages
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
}