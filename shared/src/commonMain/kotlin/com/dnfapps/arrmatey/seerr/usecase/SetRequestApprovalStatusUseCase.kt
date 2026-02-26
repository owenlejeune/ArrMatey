package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SetRequestApprovalStatusUseCase {
    operator fun invoke(
        requestId: Long,
        approvalStatus: ApprovalStatus,
        repository: SeerrInstanceRepository
    ): Flow<OperationStatus> = flow {
        emit(OperationStatus.InProgress)
        repository.setRequestStatus(requestId, approvalStatus)
            .onSuccess { emit(OperationStatus.Success()) }
            .onError { code, message, cause -> emit(OperationStatus.Error(code, message, cause)) }
    }
}