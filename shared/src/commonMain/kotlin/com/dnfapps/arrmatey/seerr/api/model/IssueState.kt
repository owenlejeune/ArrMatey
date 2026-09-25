package com.dnfapps.arrmatey.seerr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class IssueState(
    val value: String,
    val resource: StringResource,
) {
    All("all", MR.strings.all),
    Open("open", MR.strings.open),
    Closed("closed", MR.strings.closed),
}
