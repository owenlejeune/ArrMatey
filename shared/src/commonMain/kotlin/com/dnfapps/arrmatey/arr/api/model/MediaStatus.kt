package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.SerialName

enum class MediaStatus(
    val resource: StringResource,
) {
    // Sonarr Statuses
    @SerialName("continuing")
    Continuing(MR.strings.continuing),

    @SerialName("ended")
    Ended(MR.strings.ended),

    @SerialName("upcoming")
    Upcoming(MR.strings.upcoming),

    // Radarr Statuses
    @SerialName("tba")
    Tba(MR.strings.tba),

    @SerialName("announced")
    Announced(MR.strings.announced),

    @SerialName("inCinemas")
    InCinemas(MR.strings.in_cinemas),

    @SerialName("released")
    Released(MR.strings.released),

    // Shared
    @SerialName("deleted")
    Deleted(MR.strings.deleted),
    ;

    companion object {
        fun seriesValues() = listOf(Continuing, Ended, Upcoming, Deleted)

        fun movieValues() = listOf(Tba, Announced, InCinemas, Released, Deleted)
    }
}
