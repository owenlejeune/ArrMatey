package com.dnfapps.arrmatey.client.paging

import com.dnfapps.arrmatey.client.ErrorType
import com.dnfapps.arrmatey.client.NetworkException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PagingController<T: Any>(
    private val scope: CoroutineScope,
    private val sourceFactory: () -> PagingSource<T>
) {
    private val _state = MutableStateFlow(PagedData<T>())
    val state: StateFlow<PagedData<T>> = _state.asStateFlow()

    private var pagingSource: PagingSource<T>? = null

    fun loadInitialPage() {
        if (_state.value.isLoading) return
        pagingSource = sourceFactory()
        scope.launch {
            _state.update {
                PagedData(isLoading = true, error = null)
            }

            when (val result = pagingSource?.load(1)) {
                is LoadResult.Page -> {
                    _state.update {
                        PagedData(
                            items = result.data,
                            totalItemCount = result.totalItemCount,
                            currentPage = result.currentPage,
                            hasMore = result.hasNextPage,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is LoadResult.Error -> {
                    _state.update {
                        PagedData(
                            isLoading = false,
                            error = formatError(result.throwable)
                        )
                    }
                }
                null -> {
                    _state.update {
                        PagedData(
                            isLoading = false,
                            error = "Failed to create paging source"
                        )
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        val currentState = _state.value

        if (!currentState.canLoadMore) return

        scope.launch {
            _state.update { it.copy(isLoadingMore = true) }

            val nextPage = currentState.currentPage + 1

            when (val result = pagingSource?.load(nextPage)) {
                is LoadResult.Page -> {
                    _state.update {
                        it.copy(
                            items = it.items + result.data,
                            currentPage = result.currentPage,
                            hasMore = result.hasNextPage,
                            isLoadingMore = false,
                            error = null
                        )
                    }
                }
                is LoadResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoadingMore = false,
                            error = formatError(result.throwable, isLoadingMore = true)
                        )
                    }
                }
                null -> {
                    _state.update {
                        it.copy(isLoadingMore = false)
                    }
                }
            }
        }
    }

    fun refresh() {
        loadInitialPage()
    }

    fun retry() {
        if (_state.value.items.isEmpty()) {
            refresh()
        } else {
            loadNextPage()
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun invalidate() {
        scope.launch {
            pagingSource?.invalidate()
        }
    }

    private fun formatError(throwable: Throwable, isLoadingMore: Boolean = false): String {
        val prefix = if (isLoadingMore) "Failed to load more" else ""

        return when (val error = throwable as? NetworkException) {
            is NetworkException -> {
                val message = when (error.errorType) {
                    ErrorType.Network -> "Network error - check your connection"
                    ErrorType.Http -> "Server error - try again later"
                    ErrorType.Timeout -> "Request timed out"
                    else -> error.message ?: "Unknown error"
                }
                if (prefix.isNotEmpty()) "$prefix - $message" else message
            }
            else -> {
                val message = throwable.message ?: "Unknown error"
                if (prefix.isNotEmpty()) "$prefix - $message" else message
            }
        }
    }
}