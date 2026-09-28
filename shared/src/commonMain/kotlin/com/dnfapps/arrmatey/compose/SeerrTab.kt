package com.dnfapps.arrmatey.compose

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.issues
import com.dnfapps.arrmatey.shared.requests
import dev.icerock.moko.resources.StringResource

enum class SeerrTab(
    val resource: StringResource,
) {
    Requests(MR.strings.requests),
    Issues(MR.strings.issues),
}
