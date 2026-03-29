package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class SubmitIssueCommentUseCase(
    private val instanceManager: InstanceManager
) {
    operator fun invoke(issueId: Long, comment: String): Flow<OperationStatus> = flow {
        val repository = instanceManager.getSelectedSeerrRepository()
            .firstOrNull()
        if (repository == null) {
            emit(OperationStatus.Error(message = "No selected seerr repository found"))
            return@flow
        }

        emit(OperationStatus.InProgress)

        repository.submitIssueComment(issueId, comment)
            .onSuccess {
                emit(OperationStatus.Success(
                    message = "Comment submitted successfully",
                    result = it
                ))
            }
            .onError { code, message, cause ->
                emit(OperationStatus.Error(code, message, cause))
            }
    }
}