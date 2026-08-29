package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.SerialName

enum class ArtistMonitorType(
    val resource: StringResource,
) {
    @SerialName("unknown")
    Unknown(MR.strings.unknown),

    @SerialName("all")
    All(MR.strings.all_albums),

    @SerialName("future")
    Future(MR.strings.future_albums),

    @SerialName("missing")
    Missing(MR.strings.missing_albums),

    @SerialName("existing")
    Existing(MR.strings.existing_albums),

    @SerialName("first")
    FirstAlbum(MR.strings.first_album),

    @SerialName("latest")
    LatestAlbum(MR.strings.latest_album),

    @SerialName("new")
    New(MR.strings.new_albums),

    @SerialName("none")
    None(MR.strings.none),
}
