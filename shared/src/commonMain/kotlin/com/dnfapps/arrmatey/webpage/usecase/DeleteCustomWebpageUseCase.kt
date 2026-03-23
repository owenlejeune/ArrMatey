package com.dnfapps.arrmatey.webpage.usecase

import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository

class DeleteCustomWebpageUseCase(
    private val webpageRepository: CustomWebpageRepository
) {
    suspend operator fun invoke(id: Long) {
        webpageRepository.deleteWebpageById(id)
    }
}