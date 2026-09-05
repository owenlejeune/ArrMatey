package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TracearrServerType {
    @SerialName("plex") Plex,
    @SerialName("jellyfin") Jellyfin,
    @SerialName("emby") Emby,
}
