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

    private val _selectedTab = MutableStateFlow(TabItem.SHOWS)
    val selectedTab: StateFlow<TabItem> = _selectedTab.asStateFlow()

    private val _selectedDrawerTab = MutableStateFlow<TabItem?>(null)
    val selectedDrawerTab: StateFlow<TabItem?> = _selectedDrawerTab.asStateFlow()

    fun settings() = settingsNavigation

    fun arr(type: InstanceType) = when (type) {
        InstanceType.Sonarr -> seriesNavigation
        InstanceType.Radarr -> movieNavigation
        InstanceType.Lidarr -> musicNavigation
        else -> throw Exception("") // TODO - implement this for qBittorrent, sabNZB, prowlarr
    }

    fun series() = seriesNavigation

    fun movies() = movieNavigation

    fun music() = musicNavigation

    fun setSelectedTab(tab: TabItem) {
        _selectedTab.value = tab
    }

    fun setSelectedDrawerTab(tab: TabItem?) {
        _selectedDrawerTab.value = tab
    }

    fun openDrawer() {
        _drawerExpandedState.value = true
    }

    fun setDrawerOpen(isOpen: Boolean) {
        _drawerExpandedState.value = isOpen
    }

    fun openNewInstanceScreen(type: InstanceType) {
        openDrawer()
        _selectedDrawerTab.value = TabItem.SETTINGS
        settings().navigateTo(SettingsScreen.AddInstance(type))
    }

}