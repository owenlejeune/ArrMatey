package com.dnfapps.arrmatey.client.paging

data class PagingConfig(
    val pageSize: Int = 20,
    val prefetchDistance: Int = 5,
    val initialLoadSize: Int = pageSize,
)
