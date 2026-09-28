package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.activity
import com.dnfapps.arrmatey.shared.history
import com.dnfapps.arrmatey.shared.recently_downloaded
import dev.icerock.moko.resources.StringResource

enum class ActivityTabSegment(
    val resource: StringResource,
) {
    Activity(MR.strings.activity),
    History(MR.strings.history),
    Downloaded(MR.strings.recently_downloaded),
}
