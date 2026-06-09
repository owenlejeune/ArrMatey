package com.dnfapps.arrmatey.navigation

import com.dnfapps.arrmatey.compose.TabItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages global UI state that is not strictly part of a single navigator's backstack.
 */
class AppState {
    private val _drawerExpanded = MutableStateFlow(false)
    val drawerExpanded: StateFlow<Boolean> = _drawerExpanded.asStateFlow()

    private val _selectedTab = MutableStateFlow<TabItem?>(null)
    val selectedTab: StateFlow<TabItem?> = _selectedTab.asStateFlow()

    private val _overlayTab = MutableStateFlow<TabItem?>(null)
    val overlayTab: StateFlow<TabItem?> = _overlayTab.asStateFlow()

    fun setDrawerOpen(isOpen: Boolean) {
        _drawerExpanded.value = isOpen
    }

    fun setSelectedTab(tab: TabItem) {
        _selectedTab.value = tab
        _overlayTab.value = null
    }

    fun openOverlay(tab: TabItem?) {
        _overlayTab.value = tab
        _drawerExpanded.value = false
    }

    fun closeOverlay() {
        _overlayTab.value = null
    }
}
