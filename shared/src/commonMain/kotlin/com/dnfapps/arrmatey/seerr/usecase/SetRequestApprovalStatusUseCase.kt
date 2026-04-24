package com.dnfapps.arrmatey.seerr.usecase

import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SetRequestApprovalStatusUseCase {
    suspend operator fun invoke(
        requestId: Long,
        approvalStatus: ApprovalStatus,
        repository: SeerrInstanceRepository,
        profileId: Long? = null,
        rootFolder: String? = null,
        languageProfileId: Long? = null,
        seasons: List<Int>? = null
    ): NetworkResult<MediaRequest> = repository.setRequestStatus(requestId, approvalStatus, profileId, rootFolder, languageProfileId, seasons)
}