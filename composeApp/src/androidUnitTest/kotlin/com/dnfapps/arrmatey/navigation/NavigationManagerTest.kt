package com.dnfapps.arrmatey.navigation

import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.instances.model.InstanceType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationManagerTest {

    private val appState = mockk<AppState> {
        every { drawerExpanded } returns MutableStateFlow(false)
        every { selectedTab } returns MutableStateFlow(null)
        every { overlayTab } returns MutableStateFlow(null)
    }
    private val tabManager = mockk<TabManager>()
    
    private val navigationManager = NavigationManager(
        tabNavigators = emptyMap(),
        settings = mockk(),
        requests = mockk(),
        discover = mockk(),
        calendar = mockk(),
        dashboard = mockk(),
        bazarr = mockk(),
        appState = appState,
        tabManager = tabManager
    )

    @Test
    fun testTabForInstanceType() {
        assertEquals(TabItem.Standard.SHOWS, navigationManager.tabFor(InstanceType.Sonarr))
        assertEquals(TabItem.Standard.MOVIES, navigationManager.tabFor(InstanceType.Radarr))
        assertEquals(TabItem.Standard.MUSIC, navigationManager.tabFor(InstanceType.Lidarr))
        assertEquals(TabItem.Standard.BOOKS, navigationManager.tabFor(InstanceType.Booksehelf))
        assertEquals(TabItem.Standard.AUDIOBOOKS, navigationManager.tabFor(InstanceType.Listenarr))
    }
}
