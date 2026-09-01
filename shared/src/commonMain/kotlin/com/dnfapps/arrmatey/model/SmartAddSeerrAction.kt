package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class SmartAddSeerrAction(
    val resource: StringResource,
) {
    Approve(MR.strings.approve),
    Decline(MR.strings.decline),
    AlwaysAsk(MR.strings.always_ask),
    ;

    companion object {
        val default = AlwaysAsk
    }
}
