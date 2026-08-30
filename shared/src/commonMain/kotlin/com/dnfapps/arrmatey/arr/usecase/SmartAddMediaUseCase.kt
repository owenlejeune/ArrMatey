package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataResponse
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.usecase.SetRequestApprovalStatusUseCase

class SmartAddMediaUseCase(
    private val addMediaItemUseCase: AddMediaItemUseCase,
    private val setRequestApprovalStatusUseCase: SetRequestApprovalStatusUseCase,
) {
    suspend operator fun invoke(
        instanceType: InstanceType,
        item: ArrMedia,
        metadata: AudiobookMetadataResponse? = null,
        searchOnAdd: Boolean = false,
        seerrMediaDetails: RequestMediaDetails? = null,
        seerrRepository: SeerrInstanceRepository? = null,
        targetInstanceId: Long? = null,
    ) {
        addMediaItemUseCase(
            instanceType = instanceType,
            item = item,
            metadata = metadata,
            searchOnAdd = searchOnAdd,
            targetInstanceId = targetInstanceId,
        )
        approvePendingSeerrRequest(seerrMediaDetails, seerrRepository)
    }

    suspend operator fun invoke(
        instanceType: InstanceType,
        repository: ArrInstanceRepository,
        item: ArrMedia,
        metadata: AudiobookMetadataResponse? = null,
        searchOnAdd: Boolean = false,
        seerrMediaDetails: RequestMediaDetails? = null,
        seerrRepository: SeerrInstanceRepository? = null,
    ) {
        addMediaItemUseCase(
            instanceType = instanceType,
            repository = repository,
            item = item,
            metadata = metadata,
            searchOnAdd = searchOnAdd,
        )
        approvePendingSeerrRequest(seerrMediaDetails, seerrRepository)
    }

    private suspend fun approvePendingSeerrRequest(
        seerrMediaDetails: RequestMediaDetails?,
        seerrRepository: SeerrInstanceRepository?,
    ) {
        if (seerrMediaDetails != null && seerrRepository != null) {
            val pendingRequest = seerrMediaDetails.mediaInfo?.requests?.firstOrNull { it.status == 1 }
            if (pendingRequest != null) {
                setRequestApprovalStatusUseCase(
                    requestId = pendingRequest.id,
                    approvalStatus = ApprovalStatus.Approve,
                    repository = seerrRepository,
                )
            }
        }
    }
}
