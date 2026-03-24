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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.datastore.TabPreferences
import com.dnfapps.arrmatey.entensions.TabItemIconView
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.navigation.DoubleBackToExit
import com.dnfapps.arrmatey.ui.tabs.ActivityTab
import com.dnfapps.arrmatey.ui.tabs.ArrTab
import com.dnfapps.arrmatey.ui.tabs.CalendarTab
import com.dnfapps.arrmatey.ui.tabs.DownloadsTab
import com.dnfapps.arrmatey.ui.tabs.ProwlarrTab
import com.dnfapps.arrmatey.ui.tabs.RequestsTab
import com.dnfapps.arrmatey.ui.tabs.SettingsTabNavHost
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeScreen(
    navigationManager: NavigationManager = koinInject(),
    preferencesStore: PreferencesStore = koinInject(),
    activityQueue: ActivityQueueViewModel = koinInject(),
    tabManager: TabManager = koinInject(),
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val activityQueueIssuesCount by activityQueue.tasksWithIssues.collectAsStateWithLifecycle()

    val drawerExtendedState by navigationManager.drawerExpandedState.collectAsStateWithLifecycle()
    val overlayTab by navigationManager.overlayTab.collectAsStateWithLifecycle()
    val selectedTab by navigationManager.selectedTab.collectAsStateWithLifecycle()

    val useServiceNavIcons by preferencesStore.useServiceNavLogos.collectAsStateWithLifecycle(false)
    val tabConfig by tabManager.tabConfiguration.collectAsStateWithLifecycle()
    val visibleTabs = tabConfig.visibleTabs
    val drawerTabs = tabConfig.drawerTabs//.filter { it != TabItem.Standard.SETTINGS }

    val pagerState = rememberPagerState { visibleTabs.size }

    LaunchedEffect(visibleTabs, overlayTab) {
        if (overlayTab == null) {
            navigationManager.setSelectedTab(visibleTabs.first())
        }
    }

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
                    useServiceNavIcons = useServiceNavIcons,
                    activityQueueIssuesCount = activityQueueIssuesCount,
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
                            navigationManager.openOverlay(TabItem.Settings)
                            drawerState.close()
                        }
                    }
                )
            }
        },
        gesturesEnabled = false
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
                    useServiceNavIcons = useServiceNavIcons,
                    activityQueueIssuesCount = activityQueueIssuesCount,
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
    useServiceNavIcons: Boolean,
    activityQueueIssuesCount: Int,
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
                label = {
                    when (item) {
                        is TabItem.Standard -> Text(mokoString(item.resource))
                        is TabItem.CustomWebpage -> Text(item.name)
                        else -> {}
                    }
                },
                selected = overlayTab == item,
                icon = {
                    when (item) {
                        is TabItem.Standard -> {
                            TabItemIconView(item, useServiceNavIcons, activityQueueIssuesCount)
                        }
                        is TabItem.CustomWebpage -> {
                            Icon(Icons.Default.Language, contentDescription = null)
                        }
                        else -> {}
                    }
                },
                onClick = { onDrawerTabClick(item) },
            )
        }

        Spacer(Modifier.weight(1f))

        HorizontalDivider()
        NavigationDrawerItem(
            selected = overlayTab == TabItem.Settings,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text(mokoString(MR.strings.settings)) },
            onClick = onSettingsClick
        )
    }
}

@Composable
private fun MainNavigationContent(
    useServiceNavIcons: Boolean,
    activityQueueIssuesCount: Int,
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
                                when (entry) {
                                    is TabItem.Standard -> {
                                        TabItemIconView(
                                            tabItem = entry,
                                            useServiceNavIcons = useServiceNavIcons,
                                            activityQueueIssuesCount = activityQueueIssuesCount
                                        )
                                    }
                                    is TabItem.CustomWebpage -> {
                                        Icon(Icons.Default.Language, contentDescription = entry.name)
                                    }
                                    else -> {}
                                }
                            },
                            label = {
                                when (entry) {
                                    is TabItem.Standard -> Text(text = mokoString(entry.resource))
                                    is TabItem.CustomWebpage -> Text(text = entry.name)
                                    else -> {}
                                }
                            }
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
            beyondViewportPageCount = visibleTabs.size, //0
            key = { page -> visibleTabs[page].key }
        ) { page ->
            TabItemContent(visibleTabs[page])
        }
    }
}

@Composable
private fun TabItemContent(tab: TabItem) {
    when (tab) {
        is TabItem.Standard -> {
            StandardTabContent(tab)
        }
        is TabItem.CustomWebpage -> {
            key(tab.id) {
                CustomWebpageViewerScreen(webpageId = tab.id)
            }
        }
        is TabItem.Settings -> SettingsTabNavHost()
    }
}

@Composable
private fun StandardTabContent(tab: TabItem.Standard) {
    when (tab) {
        TabItem.Standard.SHOWS -> ArrTab(InstanceType.Sonarr)
        TabItem.Standard.MOVIES -> ArrTab(InstanceType.Radarr)
        TabItem.Standard.MUSIC -> ArrTab(InstanceType.Lidarr)
        TabItem.Standard.ACTIVITY -> ActivityTab()
        TabItem.Standard.DOWNLOADS -> DownloadsTab()
        TabItem.Standard.CALENDAR -> CalendarTab()
        TabItem.Standard.REQUESTS -> RequestsTab()
        TabItem.Standard.PROWLARR -> ProwlarrTab()
    }
}