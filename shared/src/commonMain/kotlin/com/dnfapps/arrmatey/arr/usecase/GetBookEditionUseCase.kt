package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.BookEdition
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.networking.NetworkResult

class GetBookEditionUseCase {
    suspend operator fun invoke(
        bookId: Long,
        repository: ArrInstanceRepository,
    ): NetworkResult<List<BookEdition>> = repository.getBookEditions(bookId)
}
