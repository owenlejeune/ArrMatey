package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class AppColor(
    val resource: StringResource,
) {
    Dynamic(MR.strings.dynamic),
    ArrMatey(MR.strings.arrmatey),
    Amoled(MR.strings.amoled),
}
