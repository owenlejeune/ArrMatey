package com.dnfapps.arrmatey.webpage.usecase

import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.webpage.model.CustomWebpage
import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository

class UpdateCustomWebpageUseCase(
    private val webpageRepository: CustomWebpageRepository
) {
    suspend operator fun invoke(webpage: CustomWebpage): InsertResult {
        return webpageRepository.updateWebpage(webpage)
    }
}