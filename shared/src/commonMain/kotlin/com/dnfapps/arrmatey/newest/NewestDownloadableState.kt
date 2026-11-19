package com.dnfapps.arrmatey.newest

import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult

/** A grouped release, with its Seerr title when Seerr is configured and found it. */
data class NewestDownloadableItem(
    val release: NewestRelease,
    val match: DiscoverResult? = null,
) {
    val key: String
        get() = NewestReleaseGrouper.keyOf(release.parsed, release.kind)

    val displayTitle: String
        get() = match?.title ?: match?.name ?: release.parsed.title

    val displayYear: Int?
        get() = (match?.releaseDate ?: match?.firstAirDate)?.take(4)?.toIntOrNull() ?: release.parsed.year

    val isInLibrary: Boolean
        get() = match?.mediaInfo?.status == AVAILABLE_STATUS

    companion object {
        /** Seerr's media status for a title that is fully available. */
        const val AVAILABLE_STATUS = 5
    }
}

sealed interface NewestDownloadableState {
    object Initial : NewestDownloadableState

    object Loading : NewestDownloadableState

    data class Success(
        val items: List<NewestDownloadableItem>,
    ) : NewestDownloadableState

    data class Error(
        val message: String,
    ) : NewestDownloadableState
}
