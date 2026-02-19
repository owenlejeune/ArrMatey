package com.dnfapps.arrmatey.compose.utils

import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class FilterBy(
    val resource: StringResource
) {
    All(MR.strings.all),
    Monitored(MR.strings.monitored),
    Unmonitored(MR.strings.unmonitored),
    Missing(MR.strings.missing),

    // Movies
    Wanted(MR.strings.wanted),
    Downloaded(MR.strings.downloaded),

    // Series
    ContinuingOnly(MR.strings.continuing_only),
    EndedOnly(MR.strings.ended_only);

    companion object {
        fun typeEntries(type: InstanceType) =
            when (type) {
                InstanceType.Sonarr -> listOf(All, Monitored, Unmonitored, Missing, ContinuingOnly, EndedOnly)
                InstanceType.Radarr -> listOf(All, Monitored, Unmonitored, Missing, Wanted, Downloaded)
                InstanceType.Lidarr -> listOf(All, Monitored, Unmonitored, Missing)
                InstanceType.Prowlarr -> emptyList()
            }
    }
}