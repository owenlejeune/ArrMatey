package com.dnfapps.arrmatey.navigation

import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.instances.model.InstanceType
import org.junit.Test
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
    fun testSetDrawerGesturesEnabled() {
        assertTrue(appState.drawerGesturesEnabled.value)

        appState.setDrawerGesturesEnabled(false)
        assertFalse(appState.drawerGesturesEnabled.value)

        appState.setDrawerGesturesEnabled(true)
        assertTrue(appState.drawerGesturesEnabled.value)
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

    @Test
    fun testAddInstanceSheet() {
        assertNull(appState.addInstanceSheetType.value)
        appState.openAddInstanceSheet(InstanceType.Sonarr)
        assertEquals(InstanceType.Sonarr, appState.addInstanceSheetType.value)

        appState.closeAddInstanceSheet()
        assertNull(appState.addInstanceSheetType.value)
    }
}
