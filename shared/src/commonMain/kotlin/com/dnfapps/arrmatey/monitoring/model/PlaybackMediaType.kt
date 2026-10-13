package com.dnfapps.arrmatey.monitoring.model

import com.dnfapps.arrmatey.seerr.api.model.RequestType

enum class PlaybackMediaType {
    Movie,
    Episode,
    Track,
    Other,
    ;

    val requestType: RequestType?
        get() = when (this) {
            Movie -> RequestType.Movie
            Episode -> RequestType.Tv
            else -> null
        }
}
