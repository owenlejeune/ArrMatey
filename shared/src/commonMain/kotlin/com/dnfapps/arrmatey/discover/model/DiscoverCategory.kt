package com.dnfapps.arrmatey.discover.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class DiscoverCategory(
    val title: StringResource,
) {
    TRENDING(MR.strings.trending),
    POPULAR_MOVIES(MR.strings.popular_movies),
    POPULAR_SERIES(MR.strings.popular_series),
    UPCOMING_MOVIES(MR.strings.upcoming_movies),
    UPCOMING_SERIES(MR.strings.upcoming_series),
}
