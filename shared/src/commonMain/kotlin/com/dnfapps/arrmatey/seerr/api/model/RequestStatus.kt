package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class RequestStatus(
    val value: Int,
    val resource: StringResource
) {
    Pending(1, MR.strings.pending),
    Approved(2, MR.strings.approved),
    Declined(3, MR.strings.declined),
    Available(5, MR.strings.available);

    companion object {
        fun fromValue(value: Int) =
            entries.firstOrNull { it.value == value } ?: Pending
    }
}