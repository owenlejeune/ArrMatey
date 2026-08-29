package com.dnfapps.arrmatey.entensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.dnfapps.arrmatey.client.paging.LazyPagingItems
import com.dnfapps.arrmatey.client.paging.PagedData
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T : Any> StateFlow<PagedData<T>>.collectAsLazyPagingItems(
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    prefetchDistance: Int = 3,
): LazyPagingItems<T> {
    val pagedDataState = collectAsState()

    return remember(pagedDataState, onLoadMore, onRefresh, prefetchDistance) {
        LazyPagingItems(
            pagedDataState = pagedDataState,
            onLoadMore = onLoadMore,
            onRefresh = onRefresh,
            prefetchDistance = prefetchDistance,
        )
    }
}
