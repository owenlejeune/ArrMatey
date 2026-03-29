package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class IssueStatus (
    val value: Int,
    val resource: StringResource
) {
    Open(1, MR.strings.open),
    Unknown(-1, MR.strings.unknown);

    companion object {
        fun fromValue(value: Int) = IssueStatus.entries.firstOrNull { it.value == value } ?: Unknown
    }
}