package com.dnfapps.arrmatey.compose.utils

import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR.strings
import com.dnfapps.arrmatey.shared.all
import com.dnfapps.arrmatey.shared.continuing_only
import com.dnfapps.arrmatey.shared.downloaded
import com.dnfapps.arrmatey.shared.ended_only
import com.dnfapps.arrmatey.shared.missing
import com.dnfapps.arrmatey.shared.monitored
import com.dnfapps.arrmatey.shared.unmonitored
import com.dnfapps.arrmatey.shared.wanted
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

@Serializable
enum class FilterBy(
    val resource: StringResource,
) {
    All(strings.all),
    Monitored(strings.monitored),
    Unmonitored(strings.unmonitored),
    Missing(strings.missing),

    // Movies
    Wanted(strings.wanted),
    Downloaded(strings.downloaded),

    // Series
    ContinuingOnly(strings.continuing_only),
    EndedOnly(strings.ended_only),
    ;

    companion object {
        fun typeEntries(type: InstanceType) = when (type) {
            InstanceType.Sonarr -> listOf(All, Monitored, Unmonitored, Missing, ContinuingOnly, EndedOnly)
            InstanceType.Radarr -> listOf(All, Monitored, Unmonitored, Missing, Wanted, Downloaded)
            InstanceType.Lidarr -> listOf(All, Monitored, Unmonitored, Missing)
            InstanceType.Bookshelf -> listOf(All, Monitored, Unmonitored, Missing)
            InstanceType.Listenarr -> listOf(All, Monitored, Unmonitored, Missing)
            InstanceType.Chaptarr -> listOf(All, Monitored, Unmonitored, Missing)
            else -> emptyList()
        }
    }
}
