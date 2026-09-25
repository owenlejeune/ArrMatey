package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class RequestState(
    val value: String,
    val resource: StringResource,
) {
    All("all", MR.strings.all),
    Pending("pending", MR.strings.pending),
    Approved("approved", MR.strings.approved),
    Completed("completed", MR.strings.completed),
    Processing("processing", MR.strings.processing),
    Failed("failed", MR.strings.failed),
    Available("available", MR.strings.available),
    Unavailable("unavailable", MR.strings.unavailable),
    Deleted("deleted", MR.strings.deleted),
}
