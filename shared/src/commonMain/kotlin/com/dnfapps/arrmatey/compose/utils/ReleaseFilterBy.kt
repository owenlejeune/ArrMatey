package com.dnfapps.arrmatey.compose.utils

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.any
import com.dnfapps.arrmatey.shared.season_pack
import com.dnfapps.arrmatey.shared.single_episode
import dev.icerock.moko.resources.StringResource

enum class ReleaseFilterBy(
    val resource: StringResource,
) {
    Any(MR.strings.any),
    SeasonPack(MR.strings.season_pack),
    SingleEpisode(MR.strings.single_episode),
}
