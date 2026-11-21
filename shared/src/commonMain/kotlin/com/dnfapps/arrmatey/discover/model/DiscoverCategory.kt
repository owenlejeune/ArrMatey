package com.dnfapps.arrmatey.discover.model

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

/**
 * Discover's rows, in their default order. [isProwlarr] rows come from Prowlarr's newest
 * releases rather than Seerr, and are only offered when a Prowlarr instance is configured.
 * New entries are appended, so saved section preferences from older builds pick them up
 * at the end of the visible list.
 */
enum class DiscoverCategory(
    val title: StringResource,
    val isProwlarr: Boolean = false,
) {
    TRENDING(MR.strings.trending),
    POPULAR_MOVIES(MR.strings.popular_movies),
    UPCOMING_MOVIES(MR.strings.upcoming_movies),
    POPULAR_SERIES(MR.strings.popular_series),
    UPCOMING_SERIES(MR.strings.upcoming_series),
    PROWLARR_MOVIES(MR.strings.movies_from_prowlarr, isProwlarr = true),
    PROWLARR_SERIES(MR.strings.tv_from_prowlarr, isProwlarr = true),
    ;

    companion object {
        /** The rows that come from Seerr, for screens that only show Seerr data. */
        val seerrEntries: List<DiscoverCategory>
            get() = entries.filterNot { it.isProwlarr }
    }
}
