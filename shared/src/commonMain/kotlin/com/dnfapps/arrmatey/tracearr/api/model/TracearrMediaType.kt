package com.dnfapps.arrmatey.tracearr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TracearrMediaType {
    @SerialName("movie") Movie,
    @SerialName("episode") Episode,
    @SerialName("track") Track,
    @SerialName("live") Live,
    @SerialName("photo") Photo,
    @SerialName("trailer") Trailer,
    @SerialName("unknown") Unknown,
}
