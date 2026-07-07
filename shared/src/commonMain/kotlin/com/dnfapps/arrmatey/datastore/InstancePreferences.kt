package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.arr.api.model.ArtistMonitorType
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitorType
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.SeriesMonitorType
import com.dnfapps.arrmatey.arr.api.model.SeriesType
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
    val customFilterId: Int? = null,
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

    val applyGlobally: Boolean = false,

    // Add Media defaults
    val addQualityProfileId: Int? = null,
    val addRootFolderPath: String? = null,
    val addSearchOnAdd: Boolean = false,

    // Sonarr
    val addSeriesMonitor: SeriesMonitorType = SeriesMonitorType.All,
    val addSeriesType: SeriesType = SeriesType.Standard,
    val addSeriesSeasonFolder: Boolean = true,

    // Radarr
    val addMovieMonitored: Boolean = true,
    val addMovieMinimumAvailability: MediaStatus = MediaStatus.Announced,

    // Lidarr
    val addArtistMonitor: ArtistMonitorType = ArtistMonitorType.All,
    val addArtistMonitorNew: ArtistMonitorType = ArtistMonitorType.None,

    // Readarr
    val addAuthorMonitor: AuthorMonitorType = AuthorMonitorType.All,
    val addAuthorMonitorNew: AuthorMonitorType = AuthorMonitorType.All,

    // Audiobookshelf
    val addAudiobookMonitored: Boolean = true
) {
    constructor(): this(SortBy.Title)
}
