package com.dnfapps.arrmatey.navigation

import androidx.navigation3.runtime.NavKey
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.StateFlow

/**
 * Orchestrates navigation across the application.
 * Focuses on switching between feature navigators and managing global UI state.
 */
class NavigationManager(
    private val tabNavigators: Map<TabItem, Navigator<*>>,
    val settings: SettingsTabNavigator,
    val requests: RequestsTabNavigator,
    private val appState: AppState
) {
    // Reactive UI state properties
    val drawerExpandedState: StateFlow<Boolean> = appState.drawerExpanded
    val selectedTab: StateFlow<TabItem> = appState.selectedTab
    val overlayTab: StateFlow<TabItem?> = appState.overlayTab

    // UI state actions
    fun openDrawer() = appState.setDrawerOpen(true)
    fun closeDrawer() = appState.setDrawerOpen(false)
    fun setDrawerOpen(isOpen: Boolean) = appState.setDrawerOpen(isOpen)

    fun setSelectedTab(tab: TabItem) = appState.setSelectedTab(tab)

    fun navigateToTab(tab: TabItem) {
        if (tab is TabItem.Standard && tab !in TabItem.defaultStandardEntries()) {
            openOverlay(tab)
        } else {
            closeOverlay()
            setSelectedTab(tab)
        }
    }

    fun openOverlay(tab: TabItem?) = appState.openOverlay(tab)
    fun closeOverlay() = appState.closeOverlay()

    /**
     * Returns the [Navigator] for a specific [InstanceType].
     */
    fun arr(type: InstanceType): Navigator<ArrScreen> {
        return navigatorFor(tabFor(type))
    }

    fun tabFor(type: InstanceType): TabItem.Standard {
        return when (type) {
            InstanceType.Sonarr -> TabItem.Standard.SHOWS
            InstanceType.Radarr -> TabItem.Standard.MOVIES
            InstanceType.Lidarr -> TabItem.Standard.MUSIC
            InstanceType.Booksehelf -> TabItem.Standard.BOOKS
            InstanceType.Listenarr -> TabItem.Standard.AUDIOBOOKS
            else -> throw IllegalStateException("Invalid arr type $type")
        }
    }

    /**
     * Generic accessor for feature navigators.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : NavKey> navigatorFor(tab: TabItem): Navigator<T> {
        return tabNavigators[tab] as? Navigator<T>
            ?: throw IllegalArgumentException("No navigator registered for tab: $tab")
    }

    // Convenience methods for specific feature transitions
    fun openNewInstanceScreen(type: InstanceType) {
        openOverlay(TabItem.Settings)
        settings.toAddInstance(type)
    }

    fun openEditInstanceScreen(id: Long) {
        openOverlay(TabItem.Settings)
        settings.toEditInstance(id)
    }

    fun openNewDownloadClientScreen() {
        openOverlay(TabItem.Settings)
        settings.toAddDownloadClient()
    }
}
