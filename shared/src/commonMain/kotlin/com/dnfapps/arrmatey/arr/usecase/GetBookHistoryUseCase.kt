package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.state.HistoryState
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetBookHistoryUseCase {
    operator fun invoke(
        bookId: Long,
        authorId: Long,
        repository: ArrInstanceRepository
    ): Flow<HistoryState> = flow {
        emit(HistoryState.Loading)
        repository.getItemHistory(authorId, altIt = bookId)
            .onSuccess { result ->
//                val filtered = result.filter {
//                    (it as? BookshelfHistoryItem)?.bookId == bookId
//                }
                emit(HistoryState.Success(result))
            }
            .onError { _, message, _ ->
                emit(HistoryState.Error(message))
            }
    }
}