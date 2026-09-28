package com.dnfapps.arrmatey.compose.utils

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.added
import com.dnfapps.arrmatey.shared.title
import dev.icerock.moko.resources.StringResource

enum class QueueSortBy(
    val resource: StringResource,
) {
    Title(MR.strings.title),
    Added(MR.strings.added),
}
