package com.dnfapps.arrmatey.client.paging

import androidx.compose.runtime.State

class LazyPagingItems<T : Any>(
    private val pagedDataState: State<PagedData<T>>,
    private val onLoadMore: () -> Unit,
    private val onRefresh: () -> Unit,
    private val prefetchDistance: Int = 3,
) {
    private val pagedData: PagedData<T>
        get() = pagedDataState.value

    val itemList: List<T>
        get() = pagedData.items

    val itemCount: Int
        get() = pagedData.items.size

    val isLoading: Boolean
        get() = pagedData.isLoading

    val isLoadingMore: Boolean
        get() = pagedData.isLoadingMore

    val hasMore: Boolean
        get() = pagedData.hasMore

    val error: String?
        get() = pagedData.error

    val isEmpty: Boolean
        get() = pagedData.isEmpty

    operator fun get(index: Int): T? {
        // Trigger load more if approaching end
        if (index >= itemCount - prefetchDistance && pagedData.canLoadMore) {
            onLoadMore()
        }

        return itemList.getOrNull(index)
    }

    fun peek(index: Int): T? = itemList.getOrNull(index)

    fun refresh() {
        onRefresh()
    }

    fun retry() {
        if (error != null) {
            if (itemCount == 0) {
                refresh()
            } else {
                onLoadMore()
            }
        }
    }
}
