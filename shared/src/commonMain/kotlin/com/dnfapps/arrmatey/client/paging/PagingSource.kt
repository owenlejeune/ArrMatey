package com.dnfapps.arrmatey.client.paging

abstract class PagingSource<T : Any> {
    abstract suspend fun load(page: Int): LoadResult<T>

    open suspend fun invalidate() {
        // override if needed
    }
}
