package com.dnfapps.arrmatey.discover.model

import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.popularity
import com.dnfapps.arrmatey.shared.rating
import com.dnfapps.arrmatey.shared.release_date
import com.dnfapps.arrmatey.shared.title
import dev.icerock.moko.resources.StringResource

enum class DiscoverSortBy(val resource: StringResource) {
    POPULARITY(MR.strings.popularity),
    RATING(MR.strings.rating),
    RELEASE_DATE(MR.strings.release_date),
    TITLE(MR.strings.title),
}

data class DiscoverFilterState(
    val sortBy: DiscoverSortBy = DiscoverSortBy.POPULARITY,
    val sortOrder: SortOrder = SortOrder.Desc,
    val selectedGenreId: Int? = null,
    val selectedLanguage: String? = null,
    val hideInLibrary: Boolean = false,
) {
    val isActive: Boolean
        get() = sortBy != DiscoverSortBy.POPULARITY ||
            sortOrder != SortOrder.Desc ||
            selectedGenreId != null ||
            selectedLanguage != null ||
            hideInLibrary
}
