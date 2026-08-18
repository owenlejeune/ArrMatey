package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataResponse
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.seerr.api.model.ApprovalStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.usecase.SetRequestApprovalStatusUseCase
import com.dnfapps.networking.onSuccess

class SmartAddMediaUseCase(
    private val addMediaItemUseCase: AddMediaItemUseCase,
    private val setRequestApprovalStatusUseCase: SetRequestApprovalStatusUseCase
) {
    suspend operator fun invoke(
        instanceType: InstanceType,
        item: ArrMedia,
        metadata: AudiobookMetadataResponse? = null,
        searchOnAdd: Boolean = false,
        seerrMediaDetails: RequestMediaDetails? = null,
        seerrRepository: SeerrInstanceRepository? = null
    ) {
        addMediaItemUseCase(instanceType, item, metadata, searchOnAdd)

        if (seerrMediaDetails != null && seerrRepository != null) {
            val pendingRequest = seerrMediaDetails.mediaInfo?.requests?.firstOrNull { it.status == 1 }
            if (pendingRequest != null) {
                setRequestApprovalStatusUseCase(
                    requestId = pendingRequest.id,
                    approvalStatus = ApprovalStatus.Approve,
                    repository = seerrRepository
                )
            }
        }
    }
}
