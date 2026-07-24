package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class AppTheme(val resource: StringResource) {
    System(MR.strings.system),
    Light(MR.strings.light),
    Dark(MR.strings.dark)
}