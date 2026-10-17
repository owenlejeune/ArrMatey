package com.dnfapps.arrmatey.seerr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.usecase.GetSeerrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.Issue
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.policy.SeerrPermissionPolicy
import com.dnfapps.arrmatey.seerr.state.IssueDetailsUiState
import com.dnfapps.arrmatey.seerr.usecase.CloseIssueUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetCurrentSeerrUserUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetIssueDetailsUseCase
import com.dnfapps.arrmatey.seerr.usecase.SubmitIssueCommentUseCase
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class IssueDetailsViewModel(
    issuePackage: MediaIssuePackage,
    private val submitIssueCommentUseCase: SubmitIssueCommentUseCase,
    private val getIssueDetailsUseCase: GetIssueDetailsUseCase,
    private val closeIssueUseCase: CloseIssueUseCase,
    getSeerrInstanceRepositoryUseCase: GetSeerrInstanceRepositoryUseCase? = null,
    getCurrentSeerrUserUseCase: GetCurrentSeerrUserUseCase? = null,
) : ViewModel() {
    private val _commentSubmissionStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    private val _issueCloseStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    private val _issuePackage = MutableStateFlow(issuePackage)

    private val userFlow: Flow<SeerrUser?> =
        getSeerrInstanceRepositoryUseCase?.observeSelected()
            ?.filterNotNull()
            ?.flatMapLatest { repo -> getCurrentSeerrUserUseCase?.invoke(repo) ?: repo.loggedInUser }
            ?: flowOf(null)

    val uiState =
        combine(
            _issuePackage,
            _commentSubmissionStatus,
            _issueCloseStatus,
            userFlow,
        ) { pacakge, comment, close, user ->
            IssueDetailsUiState(
                issuePackage = pacakge,
                commentSubmissionStatus = comment,
                issueCloseStatus = close,
                canManageIssues = user == null || SeerrPermissionPolicy.canManageIssues(user),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = IssueDetailsUiState(issuePackage),
        )

    init {
        viewModelScope.launch {
            getIssueDetailsUseCase(issuePackage.issue.id)
                .onSuccess { issue ->
                    _issuePackage.update {
                        it.copy(issue = issue)
                    }
                }
        }
    }

    fun submitIssueComment(comment: String) {
        viewModelScope.launch {
            submitIssueCommentUseCase(_issuePackage.value.issue.id, comment)
                .collect {
                    _commentSubmissionStatus.value = it
                    if (it is OperationStatus.Success) {
                        (it.result as? Issue)?.let { issue ->
                            _issuePackage.update { ip ->
                                ip.copy(issue = issue)
                            }
                        }
                    }
                }
        }
    }

    fun closeIssue(issueId: Long) {
        viewModelScope.launch {
            closeIssueUseCase(issueId)
                .collect { _issueCloseStatus.value = it }
        }
    }
}
