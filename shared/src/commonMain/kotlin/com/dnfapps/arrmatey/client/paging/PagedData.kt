package com.dnfapps.arrmatey.client.paging

data class PagedData<T>(
    val items: List<T> = emptyList(),
    val totalItemCount: Int = 0,
    val currentPage: Int = 0,
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null
) {
    val isEmpty: Boolean
        get() = items.isEmpty() && !isLoading && error == null

    val canLoadMore: Boolean
        get() = hasMore && !isLoadingMore && !isLoading

    constructor(): this(emptyList()) // empty ios constructor
}