package com.dnfapps.arrmatey.client.paging

sealed class LoadResult<T : Any> {
    data class Page<T : Any>(
        val data: List<T>,
        val totalItemCount: Int,
        val currentPage: Int,
        val hasNextPage: Boolean
    ) : LoadResult<T>()

    data class Error<T : Any>(
        val throwable: Throwable
    ) : LoadResult<T>()
}