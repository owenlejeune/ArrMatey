package com.dnfapps.arrmatey.compose.utils

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class ReleaseFilterBy(
    val resource: StringResource,
) {
    Any(MR.strings.any),
    SeasonPack(MR.strings.season_pack),
    SingleEpisode(MR.strings.single_episode),
}
