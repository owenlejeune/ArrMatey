package com.dnfapps.arrmatey.datastore

import com.dnfapps.arrmatey.arr.api.model.ArtistMonitorType
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitorType
import com.dnfapps.arrmatey.arr.api.model.BookMediaType
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.SeriesMonitorType
import com.dnfapps.arrmatey.arr.api.model.SeriesType
import com.dnfapps.arrmatey.compose.utils.FilterBy
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
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
    val customFilterId: Long? = null,
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
    // Chaptarr
    val addChaptarrMediaType: BookMediaType = BookMediaType.Audiobook,
    val addChaptarrAudiobookQualityProfileId: Int? = null,
    val addChaptarrAudiobookMetadataProfileId: Int? = null,
    val addChaptarrAudiobookRootFolderPath: String? = null,
    val addChaptarrAudiobookMonitorExisting: AuthorMonitorType = AuthorMonitorType.None,
    val addChaptarrAudiobookMonitorFuture: Boolean = true,
    val addChaptarrEbookQualityProfileId: Int? = null,
    val addChaptarrEbookMetadataProfileId: Int? = null,
    val addChaptarrEbookRootFolderPath: String? = null,
    val addChaptarrEbookMonitorExisting: AuthorMonitorType = AuthorMonitorType.None,
    val addChaptarrEbookMonitorFuture: Boolean = true,
    // Audiobookshelf
    val addAudiobookMonitored: Boolean = true,
    // Delete Media defaults
    val deleteDeleteFiles: Boolean = false,
    val deleteAddExclusion: Boolean = false,
) {
    constructor() : this(SortBy.Title)

    fun copyWithSeriesAddDefaults(
        monitor: SeriesMonitorType,
        type: SeriesType,
        seasonFolder: Boolean,
        qualityProfileId: Int?,
        rootFolderPath: String?,
        searchOnAdd: Boolean,
    ): InstancePreferences = copy(
        addSeriesMonitor = monitor,
        addSeriesType = type,
        addSeriesSeasonFolder = seasonFolder,
        addQualityProfileId = qualityProfileId,
        addRootFolderPath = rootFolderPath,
        addSearchOnAdd = searchOnAdd,
    )

    fun copyWithMovieAddDefaults(
        monitored: Boolean,
        minAvailability: MediaStatus,
        qualityProfileId: Int?,
        rootFolderPath: String?,
        searchOnAdd: Boolean,
    ): InstancePreferences = copy(
        addMovieMonitored = monitored,
        addMovieMinimumAvailability = minAvailability,
        addQualityProfileId = qualityProfileId,
        addRootFolderPath = rootFolderPath,
        addSearchOnAdd = searchOnAdd,
    )

    fun copyWithArtistAddDefaults(
        monitor: ArtistMonitorType,
        monitorNew: ArtistMonitorType,
        qualityProfileId: Int?,
        rootFolderPath: String?,
        searchOnAdd: Boolean,
    ): InstancePreferences = copy(
        addArtistMonitor = monitor,
        addArtistMonitorNew = monitorNew,
        addQualityProfileId = qualityProfileId,
        addRootFolderPath = rootFolderPath,
        addSearchOnAdd = searchOnAdd,
    )

    fun copyWithAuthorAddDefaults(
        monitor: AuthorMonitorType,
        monitorNew: AuthorMonitorType,
        qualityProfileId: Int?,
        rootFolderPath: String?,
        searchOnAdd: Boolean,
    ): InstancePreferences = copy(
        addAuthorMonitor = monitor,
        addAuthorMonitorNew = monitorNew,
        addQualityProfileId = qualityProfileId,
        addRootFolderPath = rootFolderPath,
        addSearchOnAdd = searchOnAdd,
    )

    fun copyWithAudiobookAddDefaults(
        monitored: Boolean,
        qualityProfileId: Int?,
        rootFolderPath: String?,
        searchOnAdd: Boolean,
    ): InstancePreferences = copy(
        addAudiobookMonitored = monitored,
        addQualityProfileId = qualityProfileId,
        addRootFolderPath = rootFolderPath,
        addSearchOnAdd = searchOnAdd,
    )

    fun copyWithChaptarrAddDefaults(
        mediaType: BookMediaType,
        audiobookQualityProfileId: Int?,
        audiobookMetadataProfileId: Int?,
        audiobookRootFolderPath: String?,
        audiobookMonitorExisting: AuthorMonitorType,
        audiobookMonitorFuture: Boolean,
        ebookQualityProfileId: Int?,
        ebookMetadataProfileId: Int?,
        ebookRootFolderPath: String?,
        ebookMonitorExisting: AuthorMonitorType,
        ebookMonitorFuture: Boolean,
        searchOnAdd: Boolean,
    ): InstancePreferences = copy(
        addChaptarrMediaType = mediaType,
        addChaptarrAudiobookQualityProfileId = audiobookQualityProfileId,
        addChaptarrAudiobookMetadataProfileId = audiobookMetadataProfileId,
        addChaptarrAudiobookRootFolderPath = audiobookRootFolderPath,
        addChaptarrAudiobookMonitorExisting = audiobookMonitorExisting,
        addChaptarrAudiobookMonitorFuture = audiobookMonitorFuture,
        addChaptarrEbookQualityProfileId = ebookQualityProfileId,
        addChaptarrEbookMetadataProfileId = ebookMetadataProfileId,
        addChaptarrEbookRootFolderPath = ebookRootFolderPath,
        addChaptarrEbookMonitorExisting = ebookMonitorExisting,
        addChaptarrEbookMonitorFuture = ebookMonitorFuture,
        addSearchOnAdd = searchOnAdd,
    )

    fun copyWithChaptarrMediaType(
        mediaType: BookMediaType,
    ): InstancePreferences = copy(
        addChaptarrMediaType = mediaType,
    )
}
