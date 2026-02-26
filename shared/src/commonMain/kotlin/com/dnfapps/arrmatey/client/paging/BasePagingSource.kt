package com.dnfapps.arrmatey.client.paging

import com.dnfapps.arrmatey.client.NetworkException
import com.dnfapps.arrmatey.client.NetworkResult

class BasePagingSource<T : Any, R : Any>(
    private val fetcher: suspend (page: Int) -> NetworkResult<R>,
    private val processor: suspend (R) -> PageResult<T>
) : PagingSource<T>() {

    override suspend fun load(page: Int): LoadResult<T> {
        return when (val result = fetcher(page)) {
            is NetworkResult.Loading -> {
                LoadResult.Error(Exception("Unexpected loading state"))
            }
            is NetworkResult.Success -> {
                try {
                    val pageResult = processor(result.data)
                    LoadResult.Page(
                        data = pageResult.items,
                        currentPage = page,
                        hasNextPage = pageResult.hasNextPage,
                        totalItemCount = pageResult.totalItemCount
                    )
                } catch (e: Exception) {
                    LoadResult.Error(e)
                }
            }
            is NetworkResult.Error -> {
                LoadResult.Error(
                    NetworkException(
                        code = result.code,
                        message = result.message,
                        cause = result.cause,
                        errorType = result.errorType
                    )
                )
            }
        }
    }
}