package com.dnfapps.arrmatey.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
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
        applyGloballyFlow
    ) { args: Array<Any> ->
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
            applyGlobally = args[13] as Boolean
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
        }
    }
}