package com.dnfapps.arrmatey.webpage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.webpage.usecase.GetCustomWebpageUseCase
import com.dnfapps.arrmatey.webpage.usecase.UpdateCustomWebpageUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomWebpageViewerViewModel(
    webpageId: Long,
    getWebpageUseCase: GetCustomWebpageUseCase,
    private val updateWebpageUseCase: UpdateCustomWebpageUseCase,
) : ViewModel() {
    val webpage =
        getWebpageUseCase(webpageId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    fun updateUrl(newUrl: String) {
        viewModelScope.launch {
            webpage.value?.let { customWebpage ->
                val new = customWebpage.copy(url = newUrl)
                updateWebpageUseCase(new)
            }
        }
    }
}
