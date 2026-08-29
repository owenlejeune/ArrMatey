package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.IssueBody
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow

class SubmitIssueUseCase(
    private val instanceManager: InstanceManager,
) {
    operator fun invoke(issue: IssueBody): Flow<OperationStatus> =
        flow {
            val repository =
                instanceManager
                    .getSelectedSeerrRepository()
                    .firstOrNull()
            if (repository == null) {
                emit(OperationStatus.Error(message = "No selected seerr instance found"))
                return@flow
            }

            emit(OperationStatus.InProgress)
            repository
                .submitIssue(issue)
                .onSuccess { emit(OperationStatus.Success("Issue submitted successfully")) }
                .onError { code, message, cause ->
                    emit(OperationStatus.Error(code, message, cause))
                }
        }
}
