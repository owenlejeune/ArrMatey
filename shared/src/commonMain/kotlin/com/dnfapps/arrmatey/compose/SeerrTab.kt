package com.dnfapps.arrmatey.compose

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class SeerrTab(
    val resource: StringResource
) {
    Requests(MR.strings.requests),
    Issues(MR.strings.issues)
}