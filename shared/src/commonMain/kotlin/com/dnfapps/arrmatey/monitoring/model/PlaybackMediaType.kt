package com.dnfapps.arrmatey.monitoring.model

import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.utils.AspectRatio

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

    val isAudio: Boolean
        get() = this == Track

    val aspectRatio: AspectRatio
        get() = if (isAudio) AspectRatio.Cover else AspectRatio.Poster
}
