package com.dnfapps.arrmatey.compose.utils

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class QueueSortBy(
    val resource: StringResource,
) {
    Title(MR.strings.title),
    Added(MR.strings.added),
}
