package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.discover.model.DiscoverCategory
import kotlinx.serialization.Serializable

@Serializable
data class DiscoverSectionPreferences(
    val visibleCategories: List<DiscoverCategory> = DiscoverCategory.entries,
    val hiddenCategories: List<DiscoverCategory> = emptyList(),
    val hideInLibraryContent: Boolean = false,
) {
    constructor() : this(DiscoverCategory.entries, emptyList(), false)

    /** These preferences with the Prowlarr rows left out, for when no Prowlarr instance is configured. */
    fun withoutProwlarr(): DiscoverSectionPreferences = copy(
        visibleCategories = visibleCategories.filterNot { it.isProwlarr },
        hiddenCategories = hiddenCategories.filterNot { it.isProwlarr },
    )

    /**
     * Puts back the Prowlarr rows from [original] that [withoutProwlarr] left out, each in
     * the list it was in and at the same position where it still fits.
     */
    fun restoreProwlarrFrom(original: DiscoverSectionPreferences): DiscoverSectionPreferences {
        val visible = visibleCategories.toMutableList()
        original.visibleCategories.forEachIndexed { index, category ->
            if (category.isProwlarr && category !in visible) visible.add(index.coerceAtMost(visible.size), category)
        }
        val hidden = hiddenCategories + original.hiddenCategories.filter { it.isProwlarr && it !in hiddenCategories && it !in visible }
        return copy(visibleCategories = visible, hiddenCategories = hidden)
    }
}
