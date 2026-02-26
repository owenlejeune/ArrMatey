package com.dnfapps.arrmatey.client.paging

data class PageResult<T>(
    val items: List<T>,
    val totalItemCount: Int,
    val hasNextPage: Boolean
)