package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class MediaStatus(
    val value: Int,
    val resource: StringResource
) {
    Unknown(1, MR.strings.unknown),
    Pending(2, MR.strings.pending),
    Processing(3, MR.strings.processing),
    PartiallyAvailable(4, MR.strings.partially_available),
    Available(5, MR.strings.available),
    Deleted(7, MR.strings.deleted);

    companion object {
        fun fromValue(value: Int) = entries.firstOrNull { it.value == value } ?: Unknown
    }
}