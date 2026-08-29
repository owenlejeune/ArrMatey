package com.dnfapps.arrmatey.client.paging

sealed class PagingState<out T> {
    data object Initial : PagingState<Nothing>()

    data object Loading : PagingState<Nothing>()

    data class LoadingMore<T>(
        val items: List<T>,
    ) : PagingState<T>()

    data class Success<T>(
        val items: List<T>,
        val currentPage: Int,
        val hasMore: Boolean,
    ) : PagingState<T>()

    data class Error(
        val message: String,
    ) : PagingState<Nothing>()
}

fun <T> PagedData<T>.toPagingState(): PagingState<T> =
    when {
        error != null -> PagingState.Error(error)
        isLoading && items.isEmpty() -> PagingState.Loading
        isLoadingMore -> PagingState.LoadingMore(items)
        items.isNotEmpty() ->
            PagingState.Success(
                items = items,
                currentPage = currentPage,
                hasMore = hasMore,
            )
        else -> PagingState.Initial
    }
