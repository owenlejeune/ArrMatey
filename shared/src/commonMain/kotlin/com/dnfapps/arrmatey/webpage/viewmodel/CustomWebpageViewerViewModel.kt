package com.dnfapps.arrmatey.webpage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class CustomWebpageViewerViewModel(
    private val webpageId: Long,
    private val webpageRepository: CustomWebpageRepository
): ViewModel() {

    val webpage = flow {
        val page = webpageRepository.getWebpageById(webpageId)
        emit(page)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

}