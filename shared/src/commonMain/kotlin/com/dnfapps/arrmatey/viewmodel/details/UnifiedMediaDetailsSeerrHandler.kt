package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaBody
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.usecase.CancelRequestUseCase
import com.dnfapps.arrmatey.seerr.usecase.ClearSeerrMediaDataUseCase
import com.dnfapps.arrmatey.seerr.usecase.MarkSeerrMediaAsAvailableUseCase
import com.dnfapps.arrmatey.seerr.usecase.RemoveSeerrMediaFileUseCase
import com.dnfapps.arrmatey.seerr.usecase.SetRequestApprovalStatusUseCase
import com.dnfapps.arrmatey.seerr.usecase.SubmitRequestUseCase
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UnifiedMediaDetailsSeerrHandler(
    private val submitRequestUseCase: SubmitRequestUseCase,
    private val cancelRequestUseCase: CancelRequestUseCase,
    private val setRequestApprovalStatusUseCase: SetRequestApprovalStatusUseCase,
    private val removeSeerrMediaFileUseCase: RemoveSeerrMediaFileUseCase,
    private val clearSeerrMediaDataUseCase: ClearSeerrMediaDataUseCase,
    private val markSeerrMediaAsAvailableUseCase: MarkSeerrMediaAsAvailableUseCase,
) {
    private val _isRequestSheetVisible = MutableStateFlow(false)
    val isRequestSheetVisible: StateFlow<Boolean> = _isRequestSheetVisible.asStateFlow()

    private val _isViewRequestSheetVisible = MutableStateFlow(false)
    val isViewRequestSheetVisible: StateFlow<Boolean> = _isViewRequestSheetVisible.asStateFlow()

    private val _isRequest4k = MutableStateFlow(false)
    val isRequest4k: StateFlow<Boolean> = _isRequest4k.asStateFlow()

    private val _requestStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val requestStatus: StateFlow<OperationStatus> = _requestStatus.asStateFlow()

    fun showRequestSheet(is4k: Boolean = false) {
        _requestStatus.value = OperationStatus.Idle
        _isRequest4k.value = is4k
        _isRequestSheetVisible.value = true
    }

    fun hideRequestSheet() {
        _isRequestSheetVisible.value = false
    }

    fun showViewRequestSheet() {
        _isViewRequestSheetVisible.value = true
    }

    fun hideViewRequestSheet() {
        _isViewRequestSheetVisible.value = false
    }

    fun submitRequest(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> SeerrInstanceRepository?,
        resolvedRequestType: RequestType?,
        tmdbId: Long?,
        profileId: Long? = null,
        rootFolder: String? = null,
        languageProfileId: Long? = null,
        seasons: List<Int>? = null,
        is4k: Boolean = false,
        userId: Long? = null,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val mediaType = resolvedRequestType ?: return@launch
            val mediaId = tmdbId ?: return@launch
            val body =
                RequestMediaBody(
                    mediaType = mediaType,
                    mediaId = mediaId,
                    is4k = is4k,
                    serverId = null,
                    profileId = profileId,
                    rootFolder = rootFolder,
                    languageProfileId = languageProfileId,
                    seasons = seasons,
                    userId = userId,
                )
            _requestStatus.value = OperationStatus.InProgress
            submitRequestUseCase(body, repository)
                .onSuccess {
                    _requestStatus.value = OperationStatus.Success()
                    hideRequestSheet()
                    onSuccessRefresh()
                }.onError { code, message, cause ->
                    _requestStatus.value = OperationStatus.Error(code, message, cause)
                }
        }
    }

    fun approveRequest(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> SeerrInstanceRepository?,
        requestId: Long,
        profileId: Long? = null,
        rootFolder: String? = null,
        languageProfileId: Long? = null,
        seasons: List<Int>? = null,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            _requestStatus.value = OperationStatus.InProgress
            setRequestApprovalStatusUseCase(
                requestId = requestId,
                approvalStatus = ApprovalStatus.Approve,
                repository = repository,
                profileId = profileId,
                rootFolder = rootFolder,
                languageProfileId = languageProfileId,
                seasons = seasons,
            ).onSuccess {
                _requestStatus.value = OperationStatus.Success()
                hideViewRequestSheet()
                onSuccessRefresh()
            }.onError { code, message, cause ->
                _requestStatus.value = OperationStatus.Error(code, message, cause)
            }
        }
    }

    fun cancelRequest(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> SeerrInstanceRepository?,
        requestId: Long,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            _requestStatus.value = OperationStatus.InProgress
            cancelRequestUseCase(requestId, repository)
                .onSuccess {
                    _requestStatus.value = OperationStatus.Success()
                    onSuccessRefresh()
                }.onError { code, message, cause ->
                    _requestStatus.value = OperationStatus.Error(code, message, cause)
                }
        }
    }

    fun declineRequest(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> SeerrInstanceRepository?,
        requestId: Long,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            _requestStatus.value = OperationStatus.InProgress
            setRequestApprovalStatusUseCase(requestId, ApprovalStatus.Decline, repository)
                .onSuccess {
                    _requestStatus.value = OperationStatus.Success()
                    hideViewRequestSheet()
                    onSuccessRefresh()
                }.onError { code, message, cause ->
                    _requestStatus.value = OperationStatus.Error(code, message, cause)
                }
        }
    }

    fun deleteSeerrMediaFile(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> SeerrInstanceRepository?,
        currentMediaIdProvider: () -> Long?,
        is4k: Boolean = false,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val currentMediaId = currentMediaIdProvider() ?: return@launch
            removeSeerrMediaFileUseCase(currentMediaId, is4k, repository)
                .onSuccess { onSuccessRefresh() }
        }
    }

    fun clearSeerrMediaData(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> SeerrInstanceRepository?,
        currentMediaIdProvider: () -> Long?,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val currentMediaId = currentMediaIdProvider() ?: return@launch
            clearSeerrMediaDataUseCase(currentMediaId, repository)
                .onSuccess { onSuccessRefresh() }
        }
    }

    fun markSeerrMediaAsAvailable(
        scope: CoroutineScope,
        repositoryProvider: suspend () -> SeerrInstanceRepository?,
        currentMediaIdProvider: () -> Long?,
        is4k: Boolean = false,
        onSuccessRefresh: () -> Unit,
    ) {
        scope.launch {
            val repository = repositoryProvider() ?: return@launch
            val currentMediaId = currentMediaIdProvider() ?: return@launch
            markSeerrMediaAsAvailableUseCase(currentMediaId, is4k, repository)
                .onSuccess { onSuccessRefresh() }
        }
    }
}
