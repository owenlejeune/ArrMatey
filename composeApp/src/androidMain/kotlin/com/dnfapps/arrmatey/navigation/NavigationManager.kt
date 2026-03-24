package com.dnfapps.arrmatey.navigation

import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NavigationManager(
    private val settingsNavigation: SettingsNavigation,
    private val seriesNavigation: SeriesTabNavigation,
    private val movieNavigation: MoviesTabNavigation,
    private val musicNavigation: MusicTabNavigation
) {
    private val _drawerExpandedState = MutableStateFlow(false)
    val drawerExpandedState: StateFlow<Boolean> = _drawerExpandedState.asStateFlow()

    private val _selectedTab = MutableStateFlow<TabItem>(TabItem.Standard.SHOWS)
    val selectedTab: StateFlow<TabItem> = _selectedTab.asStateFlow()

    private val _overlayTab = MutableStateFlow<TabItem?>(null)
    val overlayTab: StateFlow<TabItem?> = _overlayTab.asStateFlow()

    fun settings() = settingsNavigation
    fun series() = seriesNavigation
    fun movies() = movieNavigation
    fun music() = musicNavigation

    fun arr(type: InstanceType) = when (type) {
        InstanceType.Sonarr -> seriesNavigation
        InstanceType.Radarr -> movieNavigation
        InstanceType.Lidarr -> musicNavigation
        else -> TODO()
    }

    fun setSelectedTab(tab: TabItem) {
        _selectedTab.value = tab
        _overlayTab.value = null
    }

    fun openOverlay(tab: TabItem?) {
        _overlayTab.value = tab
        _drawerExpandedState.value = false
    }

    fun closeOverlay() {
        _overlayTab.value = null
    }

    fun openDrawer() {
        _drawerExpandedState.value = true
    }

    fun closeDrawer() {
        _drawerExpandedState.value = false
    }

    fun setDrawerOpen(isOpen: Boolean) {
        _drawerExpandedState.value = isOpen
    }

    fun openNewInstanceScreen(type: InstanceType) {
        openOverlay(TabItem.Settings)
        settings().navigateTo(SettingsScreen.AddInstance(type))
    }

    fun openEditInstanceScreen(id: Long) {
        openOverlay(TabItem.Settings)
        settings().navigateTo(SettingsScreen.EditInstance(id))
    }

    fun openNewDownloadClientScreen() {
        openOverlay(TabItem.Settings)
        settings().navigateTo(SettingsScreen.AddDownloadClient)
    }

}