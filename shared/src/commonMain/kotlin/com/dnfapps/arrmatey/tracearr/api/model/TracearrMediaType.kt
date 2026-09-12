package com.dnfapps.arrmatey.tracearr.api.model

import com.dnfapps.arrmatey.seerr.api.model.RequestType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TracearrMediaType {
    @SerialName("movie")
    Movie,

    @SerialName("show")
    Show,

    @SerialName("episode")
    Episode,

    @SerialName("track")
    Track,

    @SerialName("live")
    Live,

    @SerialName("photo")
    Photo,

    @SerialName("trailer")
    Trailer,

    @SerialName("unknown")
    Unknown,

    ;

    val requestType: RequestType?
        get() =
            when (this) {
                Movie -> RequestType.Movie
                Show, Episode -> RequestType.Tv
                else -> null
            }
}
