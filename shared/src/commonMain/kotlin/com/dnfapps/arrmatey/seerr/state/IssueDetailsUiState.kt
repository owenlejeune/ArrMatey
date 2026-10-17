package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage

data class IssueDetailsUiState(
    val issuePackage: MediaIssuePackage,
    val commentSubmissionStatus: OperationStatus = OperationStatus.Idle,
    val issueCloseStatus: OperationStatus = OperationStatus.Idle,
    val canManageIssues: Boolean = true,
) {
    constructor(issuePackage: MediaIssuePackage) : this(issuePackage, OperationStatus.Idle, OperationStatus.Idle, true) // ios overloads constructor
}
