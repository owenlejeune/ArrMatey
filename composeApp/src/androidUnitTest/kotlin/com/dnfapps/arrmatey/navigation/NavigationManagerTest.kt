package com.dnfapps.arrmatey.navigation

import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.InstanceType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import kotlin.test.assertEquals

class NavigationManagerTest {
    private val appState =
        mockk<AppState> {
            every { drawerExpanded } returns MutableStateFlow(false)
            every { drawerGesturesEnabled } returns MutableStateFlow(true)
            every { selectedTab } returns MutableStateFlow(null)
            every { overlayTab } returns MutableStateFlow(null)
            every { addInstanceSheetType } returns MutableStateFlow(null)
        }
    private val tabManager = mockk<TabManager>()
    private val instanceRepository = mockk<InstanceRepository>()

    private val navigationManager =
        NavigationManager(
            tabNavigators = emptyMap(),
            settings = mockk(),
            requests = mockk(),
            discover = mockk(),
            calendar = mockk(),
            dashboard = mockk(),
            bazarr = mockk(),
            tracearr = mockk(),
            appState = appState,
            tabManager = tabManager,
            instanceRepository = instanceRepository,
            activity = mockk(),
        )

    @Test
    fun testTabForInstanceType() {
        assertEquals(TabItem.Standard.SHOWS, navigationManager.tabFor(InstanceType.Sonarr))
        assertEquals(TabItem.Standard.MOVIES, navigationManager.tabFor(InstanceType.Radarr))
        assertEquals(TabItem.Standard.MUSIC, navigationManager.tabFor(InstanceType.Lidarr))
        assertEquals(TabItem.Standard.BOOKS, navigationManager.tabFor(InstanceType.Bookshelf))
        assertEquals(TabItem.Standard.AUDIOBOOKS, navigationManager.tabFor(InstanceType.Listenarr))
    }

    @Test
    fun testIsCurrentScreenRoot() {
        val rootNavigator =
            mockk<Navigator<*>> {
                every { backStack } returns androidx.compose.runtime.mutableStateListOf<androidx.navigation3.runtime.NavKey>(mockk())
            }
        val subscreenNavigator =
            mockk<Navigator<*>> {
                every { backStack } returns androidx.compose.runtime.mutableStateListOf<androidx.navigation3.runtime.NavKey>(mockk(), mockk())
            }
        val navManager =
            NavigationManager(
                tabNavigators =
                mapOf(
                    TabItem.Standard.SHOWS to rootNavigator,
                    TabItem.Standard.MOVIES to subscreenNavigator,
                ),
                settings = mockk(),
                requests = mockk(),
                discover = mockk(),
                calendar = mockk(),
                dashboard = mockk(),
                bazarr = mockk(),
                tracearr = mockk(),
                appState = appState,
                tabManager = tabManager,
                instanceRepository = instanceRepository,
                activity = mockk(),
            )

        kotlin.test.assertTrue(navManager.isCurrentScreenRoot(TabItem.Standard.SHOWS))
        kotlin.test.assertFalse(navManager.isCurrentScreenRoot(TabItem.Standard.MOVIES))
        kotlin.test.assertTrue(navManager.isCurrentScreenRoot(null))
    }
}
