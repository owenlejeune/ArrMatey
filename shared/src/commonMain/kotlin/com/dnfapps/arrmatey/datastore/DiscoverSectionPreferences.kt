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
}
