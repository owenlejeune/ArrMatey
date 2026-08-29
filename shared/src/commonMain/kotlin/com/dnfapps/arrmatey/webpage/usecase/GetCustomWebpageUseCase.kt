package com.dnfapps.arrmatey.webpage.usecase

import com.dnfapps.arrmatey.webpage.model.CustomWebpage
import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository
import kotlinx.coroutines.flow.Flow

class GetCustomWebpageUseCase(
    private val webpageRepository: CustomWebpageRepository,
) {
    operator fun invoke(id: Long): Flow<CustomWebpage?> = webpageRepository.observeWebpageById(id)
}
