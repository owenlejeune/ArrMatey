package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class ActivityTabSegment(val resource: StringResource) {
    Activity(MR.strings.activity),
    History(MR.strings.history);
}
