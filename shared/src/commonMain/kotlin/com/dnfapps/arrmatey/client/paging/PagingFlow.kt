package com.dnfapps.arrmatey.client.paging

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T : Any> createPagingFlow(
    config: PagingConfig = PagingConfig(),
    pagingSource: PagingSource<T>,
): Flow<PagingState<T>> =
    flow {
        emit(PagingState.Loading)

        try {
            when (val result = pagingSource.load(1)) {
                is LoadResult.Page<*> -> {
                    emit(
                        PagingState.Success(
                            items = result.data as List<T>,
                            currentPage = result.currentPage,
                            hasMore = result.hasNextPage,
                        ),
                    )
                }
                is LoadResult.Error<*> -> {
                    emit(PagingState.Error(result.throwable.message ?: "Unknown error"))
                }
            }
        } catch (e: Exception) {
            emit(PagingState.Error(e.message ?: "Unknown error"))
        }
    }
