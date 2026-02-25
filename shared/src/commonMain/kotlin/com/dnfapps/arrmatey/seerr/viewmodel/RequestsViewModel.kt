package com.dnfapps.arrmatey.seerr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.instances.usecase.GetSeerrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.usecase.GetCurrentSeerrUserUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetRequestsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@OptIn(ExperimentalCoroutinesApi::class)
class RequestsViewModel(
    getSeerrInstanceRepositoryUseCase: GetSeerrInstanceRepositoryUseCase,
    private val getCurrentSeerrUserUseCase: GetCurrentSeerrUserUseCase,
    private val getRequestsUseCase: GetRequestsUseCase,
): ViewModel() {

    private var pagingController: PagingController<MediaRequestPackage>? = null

    private val _requestsState = MutableStateFlow<PagedData<MediaRequestPackage>>(PagedData())
    val requestsState: StateFlow<PagedData<MediaRequestPackage>> = _requestsState.asStateFlow()

    private val selectedRepository = getSeerrInstanceRepositoryUseCase
        .observeSelected()
        .filterNotNull()
        .distinctUntilChanged { old, new ->
            old.instance.id == new.instance.id
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val userState: StateFlow<SeerrUser?> = selectedRepository
        .filterNotNull()
        .flatMapLatest { repository ->
            getCurrentSeerrUserUseCase(repository)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        initializePagingController()
    }

    private fun initializePagingController() {
        viewModelScope.launch {
            selectedRepository
                .filterNotNull()
                .collect { repo ->
                    pagingController = getRequestsUseCase.createPagingController(repo, viewModelScope)
                    pagingController?.loadInitialPage()
                    pagingController?.state?.collect {
                        _requestsState.value = it
                    }
                }
        }
    }

    fun loadNextPage() {
        pagingController?.loadNextPage()
    }

    fun refresh() {
        pagingController?.refresh()
    }

    fun clearError() {
        pagingController?.clearError()
    }

    fun retry() {
        pagingController?.retry()
    }

}