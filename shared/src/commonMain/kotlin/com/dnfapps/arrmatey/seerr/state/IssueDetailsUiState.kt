package com.dnfapps.arrmatey.seerr.state

import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage

data class IssueDetailsUiState(
    val issuePackage: MediaIssuePackage,
    val commentSubmissionStatus: OperationStatus = OperationStatus.Idle,
    val issueCloseStatus: OperationStatus = OperationStatus.Idle
) {
    constructor(issuePackage: MediaIssuePackage): this(issuePackage, OperationStatus.Idle) // ios overloads constructor
}