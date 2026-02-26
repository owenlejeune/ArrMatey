package com.dnfapps.arrmatey.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.datastore.TabPreferences
import com.dnfapps.arrmatey.entensions.BadgeContent
import com.dnfapps.arrmatey.entensions.androidIcon
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.navigation.DoubleBackToExit
import com.dnfapps.arrmatey.ui.tabs.ActivityTab
import com.dnfapps.arrmatey.ui.tabs.ArrTab
import com.dnfapps.arrmatey.ui.tabs.CalendarTab
import com.dnfapps.arrmatey.ui.tabs.SeerrTab
import com.dnfapps.arrmatey.ui.tabs.SettingsTabNavHost
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeScreen(
    navigationManager: NavigationManager = koinInject(),
    preferencesStore: PreferencesStore = koinInject()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val drawerExtendedState by navigationManager.drawerExpandedState.collectAsStateWithLifecycle()
    val overlayTab by navigationManager.overlayTab.collectAsStateWithLifecycle()
    val selectedTab by navigationManager.selectedTab.collectAsStateWithLifecycle()

    val tabPreferences by preferencesStore.tabPreferences.collectAsStateWithLifecycle(TabPreferences())
    val visibleTabs = tabPreferences.bottomTabItems
    val drawerTabs = tabPreferences.hiddenTabs

    val pagerState = rememberPagerState { tabPreferences.bottomTabItems.size }

    LaunchedEffect(selectedTab, visibleTabs) {
        val index = visibleTabs.indexOf(selectedTab)
        if (index >= 0) {
            pagerState.scrollToPage(index)
        }
    }

    LaunchedEffect(drawerState.currentValue) {
        val isInternalOpen = drawerState.currentValue == DrawerValue.Open
        if (drawerExtendedState != isInternalOpen) {
            navigationManager.setDrawerOpen(isInternalOpen)
        }
    }

    LaunchedEffect(drawerExtendedState) {
        if (drawerExtendedState && drawerState.isClosed) {
            drawerState.open()
        } else if (!drawerExtendedState && drawerState.isOpen) {
            drawerState.close()
        }
    }

    DoubleBackToExit()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerState = drawerState) {
                DrawerContent(
                    drawerTabs = drawerTabs,
                    overlayTab = overlayTab,
                    onHomeClick = {
                        scope.launch {
                            navigationManager.closeOverlay()
                            navigationManager.closeDrawer()
                            drawerState.close()
                        }
                    },
                    onDrawerTabClick = { tab ->
                        scope.launch {
                            navigationManager.openOverlay(tab)
                            drawerState.close()
                        }
                    },
                    onSettingsClick = {
                        scope.launch {
                            navigationManager.openOverlay(TabItem.SETTINGS)
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        AnimatedContent(
            targetState = overlayTab,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.98f))
                    .togetherWith(fadeOut())
            },
            label = "OverlayTransition"
        ) { currentOverlay ->
            if (currentOverlay != null) {
                TabItemContent(currentOverlay)
            } else {
                MainNavigationContent(
                    visibleTabs = visibleTabs,
                    selectedTab = selectedTab,
                    pagerState = pagerState,
                    onTabSelected = { navigationManager.setSelectedTab(it) }
                )
            }
        }
    }
}

@Composable
private fun DrawerContent(
    drawerTabs: List<TabItem>,
    overlayTab: TabItem?,
    onHomeClick: () -> Unit,
    onDrawerTabClick: (TabItem) -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        NavigationDrawerItem(
            label = { Text(mokoString(MR.strings.home)) },
            selected = overlayTab == null,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            onClick = onHomeClick
        )
        HorizontalDivider()

        drawerTabs.forEach { item ->
            NavigationDrawerItem(
                label = { Text(mokoString(item.resource)) },
                selected = overlayTab == item,
                icon = { Icon(item.androidIcon, contentDescription = null) },
                onClick = { onDrawerTabClick(item) },
                badge = { BadgeContent(item) }
            )
        }

        Spacer(Modifier.weight(1f))

        HorizontalDivider()
        NavigationDrawerItem(
            selected = overlayTab == TabItem.SETTINGS,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(mokoString(MR.strings.settings)) },
            onClick = onSettingsClick
        )
    }
}

@Composable
private fun MainNavigationContent(
    visibleTabs: List<TabItem>,
    selectedTab: TabItem,
    pagerState: PagerState,
    onTabSelected: (TabItem) -> Unit
) {
    Scaffold(
        bottomBar = {
            if (visibleTabs.size > 1) {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                    visibleTabs.forEach { entry ->
                        NavigationBarItem(
                            selected = entry == selectedTab,
                            onClick = { onTabSelected(entry) },
                            icon = {
                                BadgedBox(badge = { BadgeContent(tabItem = entry) }) {
                                    Icon(
                                        imageVector = entry.androidIcon,
                                        contentDescription = mokoString(entry.resource)
                                    )
                                }
                            },
                            label = { Text(text = mokoString(entry.resource)) }
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            userScrollEnabled = false,
            beyondViewportPageCount = visibleTabs.size,
            key = { page -> visibleTabs[page].name }
        ) { page ->
            TabItemContent(visibleTabs[page])
        }
    }
}

@Composable
private fun TabItemContent(tab: TabItem) {
    when (tab) {
        TabItem.SHOWS -> ArrTab(InstanceType.Sonarr)
        TabItem.MOVIES -> ArrTab(InstanceType.Radarr)
        TabItem.MUSIC -> ArrTab(InstanceType.Lidarr)
        TabItem.ACTIVITY -> ActivityTab()
        TabItem.CALENDAR -> CalendarTab()
        TabItem.REQUESTS -> SeerrTab()

        TabItem.SETTINGS -> SettingsTabNavHost()
    }
}