package com.dnfapps.arrmatey.seerr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.client.paging.PagingController
import com.dnfapps.arrmatey.instances.usecase.GetSeerrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.state.RequestOperationsState
import com.dnfapps.arrmatey.seerr.usecase.CancelRequestUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetCurrentSeerrUserUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetRequestsUseCase
import com.dnfapps.arrmatey.seerr.usecase.RemoveSeerrMediaFileUseCase
import com.dnfapps.arrmatey.seerr.usecase.SetRequestApprovalStatusUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@OptIn(ExperimentalCoroutinesApi::class)
class RequestsViewModel(
    getSeerrInstanceRepositoryUseCase: GetSeerrInstanceRepositoryUseCase,
    private val getCurrentSeerrUserUseCase: GetCurrentSeerrUserUseCase,
    private val getRequestsUseCase: GetRequestsUseCase,
    private val setRequestApprovalStatusUseCase: SetRequestApprovalStatusUseCase,
    private val cancelRequestUseCase: CancelRequestUseCase,
    private val removeSeerrMediaFileUseCase: RemoveSeerrMediaFileUseCase
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

    fun approveRequest(requestId: Long) {
        val repository = selectedRepository.value ?: return
        viewModelScope.launch {
            setRequestApprovalStatusUseCase(requestId, ApprovalStatus.Approve, repository)
                .onSuccess { refresh() }
        }
    }

    fun declineRequest(requestId: Long) {
        val repository = selectedRepository.value ?: return
        viewModelScope.launch {
            setRequestApprovalStatusUseCase(requestId, ApprovalStatus.Decline, repository)
                .onSuccess { refresh() }
        }
    }

    fun cancelRequest(requestId: Long) {
        val repository = selectedRepository.value ?: return
        viewModelScope.launch {
            cancelRequestUseCase(requestId, repository)
                .onSuccess { refresh() }
        }
    }

    fun deleteMediaFile(request: MediaRequest) {
        val repository = selectedRepository.value ?: return
        viewModelScope.launch {
            removeSeerrMediaFileUseCase(
                request.id,
                request.media.id,
                request.is4k,
                repository
            )
                .onSuccess { refresh() }
        }
    }

}