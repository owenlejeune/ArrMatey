package com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets

import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.arrmatey.ui.sheets.SeerrViewRequestSheet

@Composable
fun SeerrViewRequestSheetHost(
    visible: Boolean,
    details: RequestMediaDetails?,
    serviceDetails: ServiceDetails?,
    requestInProgress: Boolean,
    onApproveRequest: (requestId: Long, profileId: Long?, rootFolder: String?, langId: Long?, seasons: List<Int>?) -> Unit,
    onDeclineRequest: (requestId: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    if (visible && details != null) {
        SeerrViewRequestSheet(
            details = details,
            serviceDetails = serviceDetails,
            requestInProgress = requestInProgress,
            onDismissRequest = onDismiss,
            onApproveRequest = onApproveRequest,
            onDeclineRequest = onDeclineRequest,
        )
    }
}
