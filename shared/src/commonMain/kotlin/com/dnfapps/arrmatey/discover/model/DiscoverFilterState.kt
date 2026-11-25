package com.dnfapps.arrmatey.discover.model

import kotlinx.serialization.Serializable

@Serializable
enum class DiscoverSortBy {
    POPULARITY,
    RATING,
    RELEASE_DATE,
    UPLOAD_TIME,
    SEEDERS,
}

@Serializable
enum class DiscoverSortOrder {
    ASCENDING,
    DESCENDING,
}

@Serializable
data class DiscoverFilterState(
    val sortBy: DiscoverSortBy = DiscoverSortBy.POPULARITY,
    val sortOrder: DiscoverSortOrder = DiscoverSortOrder.DESCENDING,
    /** Empty means all genres. A title passes when it has any of these. */
    val selectedGenreIds: Set<Int> = emptySet(),
    /** Empty means all languages, as lower-case codes. A title passes when its original language is any of these. */
    val selectedLanguages: Set<String> = emptySet(),
    val hideInLibrary: Boolean = false,
) {
    val isActive: Boolean
        get() = sortBy != DiscoverSortBy.POPULARITY ||
            sortOrder != DiscoverSortOrder.DESCENDING ||
            selectedGenreIds.isNotEmpty() ||
            selectedLanguages.isNotEmpty() ||
            hideInLibrary

    /** Adds or removes [genreId]. Removing the last one returns to all genres. */
    fun toggleGenre(genreId: Int): DiscoverFilterState = copy(
        selectedGenreIds = if (genreId in selectedGenreIds) selectedGenreIds - genreId else selectedGenreIds + genreId,
    )

    /** Adds or removes [language]. Removing the last one returns to all languages. */
    fun toggleLanguage(language: String): DiscoverFilterState {
        val code = language.lowercase()
        return copy(selectedLanguages = if (code in selectedLanguages) selectedLanguages - code else selectedLanguages + code)
    }

    fun matchesGenres(genreIds: List<Int>): Boolean = selectedGenreIds.isEmpty() || genreIds.any { it in selectedGenreIds }

    fun matchesLanguage(language: String?): Boolean = selectedLanguages.isEmpty() || language?.lowercase() in selectedLanguages
}
