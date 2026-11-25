package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class ProwlarrTabSegment(
    val resource: StringResource,
) {
    All(MR.strings.all),
    Movies(MR.strings.movies),
    Tv(MR.strings.prowlarr_tv),
}
