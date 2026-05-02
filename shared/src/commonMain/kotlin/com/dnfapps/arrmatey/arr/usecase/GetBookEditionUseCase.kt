package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.BookEdition
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository

class GetBookEditionUseCase {
    suspend operator fun invoke(
        bookId: Long,
        repository: ArrInstanceRepository
    ): NetworkResult<List<BookEdition>> {
        return repository.getBookEditions(bookId)
    }
}