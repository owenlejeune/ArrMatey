package com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets

import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import com.dnfapps.arrmatey.ui.sheets.SeerrRequestSheet

@Composable
fun SeerrRequestSheetHost(
    visible: Boolean,
    details: RequestMediaDetails?,
    serviceDetails: ServiceDetails?,
    currentUser: SeerrUser?,
    users: List<SeerrUser>,
    requestInProgress: Boolean,
    onSubmitRequest: (profileId: Long?, rootFolder: String?, langId: Long?, seasons: List<Int>?, userId: Long?) -> Unit,
    onDismiss: () -> Unit,
    canSwitchToAddDirectly: Boolean = false,
    instanceTypeName: String? = null,
    onSwitchToAddDirectly: (() -> Unit)? = null,
) {
    if (visible && details != null) {
        SeerrRequestSheet(
            details = details,
            serviceDetails = serviceDetails,
            currentUser = currentUser,
            users = users,
            requestInProgress = requestInProgress,
            onDismissRequest = onDismiss,
            onSubmitRequest = onSubmitRequest,
            canSwitchToAddDirectly = canSwitchToAddDirectly,
            instanceTypeName = instanceTypeName,
            onSwitchToAddDirectly = onSwitchToAddDirectly,
        )
    }
}
