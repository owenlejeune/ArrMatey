package com.dnfapps.arrmatey.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dnfapps.arrmatey.arr.api.model.ArtistMonitorType
import com.dnfapps.arrmatey.arr.api.model.AuthorMonitorType
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class InstancePreferenceStore(
    instanceId: Long,
    dataStoreFactory: DataStoreFactory
) {
    private val dataStore: DataStore<Preferences> =
        dataStoreFactory.provideInstanceDataStore(instanceId)

    private val viewTypeKey = stringPreferencesKey("viewType")
    private val sortByKey = stringPreferencesKey("sortBy")
    private val sortOrderKey = stringPreferencesKey("sortOrder")
    private val filterByKey = stringPreferencesKey("filterBy")
    private val showFullDetailsKey = booleanPreferencesKey("showFullDetails")
    private val showOverlayKey = booleanPreferencesKey("showOverlay")
    private val showBannerBackgroundKey = booleanPreferencesKey("showBannerBackground")
    private val includeOverviewKey = booleanPreferencesKey("includeOverview")
    private val bannerBlurKey = stringPreferencesKey("bannerBlur")
    private val gridDensityKey = stringPreferencesKey("gridDensity")
    private val gridSpacingKey = stringPreferencesKey("gridSpacing")
    private val posterElevationKey = stringPreferencesKey("posterElevation")
    private val posterRadiusKey = stringPreferencesKey("posterRadius")
    private val applyGloballyKey = booleanPreferencesKey("applyGlobally")
    private val customFilterIdKey = longPreferencesKey("customFilterId")

    private val addQualityProfileIdKey = intPreferencesKey("addQualityProfileId")
    private val addRootFolderPathKey = stringPreferencesKey("addRootFolderPath")
    private val addSearchOnAddKey = booleanPreferencesKey("addSearchOnAdd")

    private val addSeriesMonitorKey = stringPreferencesKey("addSeriesMonitor")
    private val addSeriesTypeKey = stringPreferencesKey("addSeriesType")
    private val addSeriesSeasonFolderKey = booleanPreferencesKey("addSeriesSeasonFolder")

    private val addMovieMonitoredKey = booleanPreferencesKey("addMovieMonitored")
    private val addMovieMinimumAvailabilityKey = stringPreferencesKey("addMovieMinimumAvailability")

    private val addArtistMonitorKey = stringPreferencesKey("addArtistMonitor")
    private val addArtistMonitorNewKey = stringPreferencesKey("addArtistMonitorNew")

    private val addAuthorMonitorKey = stringPreferencesKey("addAuthorMonitor")
    private val addAuthorMonitorNewKey = stringPreferencesKey("addAuthorMonitorNew")

    private val addAudiobookMonitoredKey = booleanPreferencesKey("addAudiobookMonitored")

    private val sortByFlow: Flow<SortBy> = dataStore.data
        .map { preferences ->
            preferences[sortByKey]?.let { SortBy.valueOf(it) } ?: SortBy.Title
        }

    private val sortOrderFlow: Flow<SortOrder> = dataStore.data
        .map { preferences ->
            preferences[sortOrderKey]?.let { SortOrder.valueOf(it) } ?: SortOrder.Asc
        }

    private val filterByFlow: Flow<FilterBy> = dataStore.data
        .map { preferences ->
            preferences[filterByKey]?.let { FilterBy.valueOf(it) } ?: FilterBy.All
        }

    private val viewTypeFlow: Flow<ViewType> = dataStore.data
        .map { preferences ->
            preferences[viewTypeKey]?.let { ViewType.valueOf(it) } ?: ViewType.Grid
        }

    private val showFullDetailsFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[showFullDetailsKey] ?: false }

    private val showOverlayFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[showOverlayKey] ?: true }

    private val showBannerBackgroundFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[showBannerBackgroundKey] ?: true }

    private val includeOverviewFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[includeOverviewKey] ?: false }

    private val bannerBlurFlow: Flow<Blur> = dataStore.data
        .map { preferences ->
            preferences[bannerBlurKey]?.let { Blur.valueOf(it) } ?: Blur.Normal
        }

    private val gridDensityFlow: Flow<GridDensity> = dataStore.data
        .map { preferences ->
            preferences[gridDensityKey]?.let { GridDensity.valueOf(it) } ?: GridDensity.Normal
        }

    private val gridSpacingFlow: Flow<GridSpacing> = dataStore.data
        .map { preferences ->
            preferences[gridSpacingKey]?.let { GridSpacing.valueOf(it) } ?: GridSpacing.Medium
        }

    private val posterElevationFlow: Flow<PosterElevation> = dataStore.data
        .map { preferences ->
            preferences[posterElevationKey]?.let { PosterElevation.valueOf(it) } ?: PosterElevation.Medium
        }

    private val posterRadiusFlow: Flow<PosterRadius> = dataStore.data
        .map { preferences ->
            preferences[posterRadiusKey]?.let { PosterRadius.valueOf(it) } ?: PosterRadius.Medium
        }

    private val applyGloballyFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[applyGloballyKey] ?: false }

    private val customFilterIdFlow: Flow<Long?> = dataStore.data
        .map { preferences -> preferences[customFilterIdKey] }

    private val addQualityProfileIdFlow: Flow<Int?> = dataStore.data
        .map { preferences -> preferences[addQualityProfileIdKey] }

    private val addRootFolderPathFlow: Flow<String?> = dataStore.data
        .map { preferences -> preferences[addRootFolderPathKey] }

    private val addSearchOnAddFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[addSearchOnAddKey] ?: false }

    private val addSeriesMonitorFlow: Flow<SeriesMonitorType> = dataStore.data
        .map { preferences ->
            preferences[addSeriesMonitorKey]?.let { SeriesMonitorType.valueOf(it) } ?: SeriesMonitorType.All
        }

    private val addSeriesTypeFlow: Flow<SeriesType> = dataStore.data
        .map { preferences ->
            preferences[addSeriesTypeKey]?.let { SeriesType.valueOf(it) } ?: SeriesType.Standard
        }

    private val addSeriesSeasonFolderFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[addSeriesSeasonFolderKey] ?: true }

    private val addMovieMonitoredFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[addMovieMonitoredKey] ?: true }

    private val addMovieMinimumAvailabilityFlow: Flow<MediaStatus> = dataStore.data
        .map { preferences ->
            preferences[addMovieMinimumAvailabilityKey]?.let { MediaStatus.valueOf(it) } ?: MediaStatus.Announced
        }

    private val addArtistMonitorFlow: Flow<ArtistMonitorType> = dataStore.data
        .map { preferences ->
            preferences[addArtistMonitorKey]?.let { ArtistMonitorType.valueOf(it) } ?: ArtistMonitorType.All
        }

    private val addArtistMonitorNewFlow: Flow<ArtistMonitorType> = dataStore.data
        .map { preferences ->
            preferences[addArtistMonitorNewKey]?.let { ArtistMonitorType.valueOf(it) } ?: ArtistMonitorType.None
        }

    private val addAuthorMonitorFlow: Flow<AuthorMonitorType> = dataStore.data
        .map { preferences ->
            preferences[addAuthorMonitorKey]?.let { AuthorMonitorType.valueOf(it) } ?: AuthorMonitorType.All
        }

    private val addAuthorMonitorNewFlow: Flow<AuthorMonitorType> = dataStore.data
        .map { preferences ->
            preferences[addAuthorMonitorNewKey]?.let { AuthorMonitorType.valueOf(it) } ?: AuthorMonitorType.All
        }

    private val addAudiobookMonitoredFlow: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[addAudiobookMonitoredKey] ?: true }

    fun observePreferences(): Flow<InstancePreferences> = combine(
        sortByFlow,
        sortOrderFlow,
        filterByFlow,
        viewTypeFlow,
        showFullDetailsFlow,
        showOverlayFlow,
        showBannerBackgroundFlow,
        includeOverviewFlow,
        bannerBlurFlow,
        gridDensityFlow,
        gridSpacingFlow,
        posterElevationFlow,
        posterRadiusFlow,
        applyGloballyFlow,
        customFilterIdFlow,
        addQualityProfileIdFlow,
        addRootFolderPathFlow,
        addSearchOnAddFlow,
        addSeriesMonitorFlow,
        addSeriesTypeFlow,
        addSeriesSeasonFolderFlow,
        addMovieMonitoredFlow,
        addMovieMinimumAvailabilityFlow,
        addArtistMonitorFlow,
        addArtistMonitorNewFlow,
        addAuthorMonitorFlow,
        addAuthorMonitorNewFlow,
        addAudiobookMonitoredFlow
    ) { args: Array<Any?> ->
        InstancePreferences(
            sortBy = args[0] as SortBy,
            sortOrder = args[1] as SortOrder,
            filterBy = args[2] as FilterBy,
            viewType = args[3] as ViewType,
            showFullDetails = args[4] as Boolean,
            showOverlay = args[5] as Boolean,
            showBannerBackground = args[6] as Boolean,
            includeOverview = args[7] as Boolean,
            bannerBlur = args[8] as Blur,
            gridDensity = args[9] as GridDensity,
            gridSpacing = args[10] as GridSpacing,
            posterElevation = args[11] as PosterElevation,
            posterRadius = args[12] as PosterRadius,
            applyGlobally = args[13] as Boolean,
            customFilterId = args[14] as? Long,
            addQualityProfileId = args[15] as? Int,
            addRootFolderPath = args[16] as? String,
            addSearchOnAdd = args[17] as Boolean,
            addSeriesMonitor = args[18] as SeriesMonitorType,
            addSeriesType = args[19] as SeriesType,
            addSeriesSeasonFolder = args[20] as Boolean,
            addMovieMonitored = args[21] as Boolean,
            addMovieMinimumAvailability = args[22] as MediaStatus,
            addArtistMonitor = args[23] as ArtistMonitorType,
            addArtistMonitorNew = args[24] as ArtistMonitorType,
            addAuthorMonitor = args[25] as AuthorMonitorType,
            addAuthorMonitorNew = args[26] as AuthorMonitorType,
            addAudiobookMonitored = args[27] as Boolean
        )
    }

    suspend fun savePreferences(preferences: InstancePreferences) {
        dataStore.edit { prefs ->
            prefs[sortByKey] = preferences.sortBy.name
            prefs[sortOrderKey] = preferences.sortOrder.name
            prefs[filterByKey] = preferences.filterBy.name
            prefs[viewTypeKey] = preferences.viewType.name
            prefs[showFullDetailsKey] = preferences.showFullDetails
            prefs[showOverlayKey] = preferences.showOverlay
            prefs[showBannerBackgroundKey] = preferences.showBannerBackground
            prefs[includeOverviewKey] = preferences.includeOverview
            prefs[bannerBlurKey] = preferences.bannerBlur.name
            prefs[gridDensityKey] = preferences.gridDensity.name
            prefs[gridSpacingKey] = preferences.gridSpacing.name
            prefs[posterElevationKey] = preferences.posterElevation.name
            prefs[posterRadiusKey] = preferences.posterRadius.name
            prefs[applyGloballyKey] = preferences.applyGlobally
            preferences.customFilterId?.let { prefs[customFilterIdKey] = it } ?: prefs.remove(customFilterIdKey)

            preferences.addQualityProfileId?.let { prefs[addQualityProfileIdKey] = it } ?: prefs.remove(addQualityProfileIdKey)
            preferences.addRootFolderPath?.let { prefs[addRootFolderPathKey] = it } ?: prefs.remove(addRootFolderPathKey)
            prefs[addSearchOnAddKey] = preferences.addSearchOnAdd

            prefs[addSeriesMonitorKey] = preferences.addSeriesMonitor.name
            prefs[addSeriesTypeKey] = preferences.addSeriesType.name
            prefs[addSeriesSeasonFolderKey] = preferences.addSeriesSeasonFolder

            prefs[addMovieMonitoredKey] = preferences.addMovieMonitored
            prefs[addMovieMinimumAvailabilityKey] = preferences.addMovieMinimumAvailability.name

            prefs[addArtistMonitorKey] = preferences.addArtistMonitor.name
            prefs[addArtistMonitorNewKey] = preferences.addArtistMonitorNew.name

            prefs[addAuthorMonitorKey] = preferences.addAuthorMonitor.name
            prefs[addAuthorMonitorNewKey] = preferences.addAuthorMonitorNew.name

            prefs[addAudiobookMonitoredKey] = preferences.addAudiobookMonitored
        }
    }
}