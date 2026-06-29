package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.compose.utils.FilterBy
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.Blur
import com.dnfapps.arrmatey.utils.GridDensity
import com.dnfapps.arrmatey.utils.GridSpacing
import com.dnfapps.arrmatey.utils.PosterElevation
import com.dnfapps.arrmatey.utils.PosterRadius
import kotlinx.serialization.Serializable

@Serializable
data class InstancePreferences(
    val sortBy: SortBy = SortBy.Title,
    val sortOrder: SortOrder = SortOrder.Asc,
    val filterBy: FilterBy = FilterBy.All,
    val viewType: ViewType = ViewType.Grid,

    val posterElevation: PosterElevation = PosterElevation.Medium,
    val posterRadius: PosterRadius = PosterRadius.Medium,

    // Grid preferences
    val showFullDetails: Boolean = false,
    val showOverlay: Boolean = true,
    val gridDensity: GridDensity = GridDensity.Normal,
    val gridSpacing: GridSpacing = GridSpacing.Medium,

    // List preferences
    val showBannerBackground: Boolean = true,
    val includeOverview: Boolean = false,
    val bannerBlur: Blur = Blur.Normal,

    val applyGlobally: Boolean = false
) {
    constructor(): this(SortBy.Title)
}