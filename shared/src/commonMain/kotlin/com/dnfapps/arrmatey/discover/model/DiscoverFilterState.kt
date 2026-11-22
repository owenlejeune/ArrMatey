package com.dnfapps.arrmatey.discover.model

enum class DiscoverSortBy {
    POPULARITY,
    RATING,
    RELEASE_DATE,
    UPLOAD_TIME,
    SEEDERS,
}

enum class DiscoverSortOrder {
    ASCENDING,
    DESCENDING,
}

data class DiscoverFilterState(
    val sortBy: DiscoverSortBy = DiscoverSortBy.POPULARITY,
    val sortOrder: DiscoverSortOrder = DiscoverSortOrder.DESCENDING,
    val selectedGenreId: Int? = null,
    val selectedLanguage: String? = null,
    val hideInLibrary: Boolean = false,
) {
    val isActive: Boolean
        get() = sortBy != DiscoverSortBy.POPULARITY ||
            sortOrder != DiscoverSortOrder.DESCENDING ||
            selectedGenreId != null ||
            selectedLanguage != null ||
            hideInLibrary
}
