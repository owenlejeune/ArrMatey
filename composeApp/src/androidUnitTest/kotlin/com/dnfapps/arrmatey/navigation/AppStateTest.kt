package com.dnfapps.arrmatey.navigation

import com.dnfapps.arrmatey.compose.TabItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppStateTest {
    private val appState = AppState()

    @Test
    fun testInitialState() {
        assertFalse(appState.drawerExpanded.value)
        assertNull(appState.selectedTab.value)
        assertNull(appState.overlayTab.value)
    }

    @Test
    fun testSetDrawerOpen() {
        appState.setDrawerOpen(true)
        assertTrue(appState.drawerExpanded.value)

        appState.setDrawerOpen(false)
        assertFalse(appState.drawerExpanded.value)
    }

    @Test
    fun testSetSelectedTab() {
        appState.setSelectedTab(TabItem.Standard.DASHBOARD)
        assertEquals(TabItem.Standard.DASHBOARD, appState.selectedTab.value)
        assertNull(appState.overlayTab.value)
    }

    @Test
    fun testOpenOverlay() {
        appState.setDrawerOpen(true)
        appState.openOverlay(TabItem.Settings)

        assertEquals(TabItem.Settings, appState.overlayTab.value)
        assertFalse(appState.drawerExpanded.value) // Should close drawer
    }
}
