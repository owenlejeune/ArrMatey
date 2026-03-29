package com.dnfapps.arrmatey.seerr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.usecase.GetSeerrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.seerr.api.model.Issue
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.seerr.state.IssueDetailsUiState
import com.dnfapps.arrmatey.seerr.usecase.CloseIssueUseCase
import com.dnfapps.arrmatey.seerr.usecase.GetIssueDetailsUseCase
import com.dnfapps.arrmatey.seerr.usecase.SubmitIssueCommentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IssueDetailsViewModel(
    issuePackage: MediaIssuePackage,
    private val submitIssueCommentUseCase: SubmitIssueCommentUseCase,
    private val getIssueDetailsUseCase: GetIssueDetailsUseCase,
    private val closeIssueUseCase: CloseIssueUseCase
): ViewModel() {

    private val _commentSubmissionStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    private val _issueCloseStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    private val _issuePackage = MutableStateFlow(issuePackage)

    val uiState = combine(
        _issuePackage,
        _commentSubmissionStatus,
        _issueCloseStatus
    ) { pacakge, comment, close ->
        IssueDetailsUiState(
            issuePackage = pacakge,
            commentSubmissionStatus = comment,
            issueCloseStatus = close
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = IssueDetailsUiState(issuePackage)
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