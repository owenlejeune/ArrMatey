package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.SerialName

enum class SeriesMonitorType(
    val resource: StringResource,
) {
    @SerialName("unknown")
    Unknown(MR.strings.unknown),

    @SerialName("all")
    All(MR.strings.all),

    @SerialName("future")
    Future(MR.strings.future),

    @SerialName("missing")
    Missing(MR.strings.missing),

    @SerialName("existing")
    Existing(MR.strings.existing),

    @SerialName("firstSeason")
    FirstSeason(MR.strings.first_season),

    @SerialName("lastSeason")
    LastSeason(MR.strings.last_season),

    @SerialName("latestSeason")
    LatestSeason(MR.strings.latest_seasons),

    @SerialName("pilot")
    Pilot(MR.strings.pilot),

    @SerialName("recent")
    Recent(MR.strings.recent),

    @SerialName("monitorSpecials")
    MonitorSpecials(MR.strings.monitor_specials),

    @SerialName("unmonitorSpecials")
    UnmonitorSpecials(MR.strings.unmonitor_specials),

    @SerialName("none")
    None(MR.strings.none),

    @SerialName("skip")
    Skip(MR.strings.skip),
}
