package com.dnfapps.arrmatey.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.IconButton
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.entensions.TabItemIconView
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.LocalNavigationManager
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.toSearch
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.navigation.DoubleBackToExit
import com.dnfapps.arrmatey.ui.tabs.ActivityTab
import com.dnfapps.arrmatey.ui.tabs.ArrTab
import com.dnfapps.arrmatey.ui.tabs.BazarrTab
import com.dnfapps.arrmatey.ui.tabs.CalendarTab
import com.dnfapps.arrmatey.ui.tabs.DashboardTab
import com.dnfapps.arrmatey.ui.tabs.DownloadsTab
import com.dnfapps.arrmatey.ui.tabs.BazarrTab
import com.dnfapps.arrmatey.ui.tabs.ProwlarrTab
import com.dnfapps.arrmatey.ui.tabs.SeerrTab
import com.dnfapps.arrmatey.ui.tabs.SettingsTabNavHost
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    navigationManager: NavigationManager = koinInject(),
    preferencesStore: PreferencesStore = koinInject(),
    activityQueue: ActivityQueueViewModel = koinInject(),
    tabManager: TabManager = koinInject(),
    instanceRepository: InstanceRepository = koinInject(),
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val activityQueueIssuesCount by activityQueue.tasksWithIssues.collectAsStateWithLifecycle()

    val allInstances by instanceRepository.allInstancesFlow.collectAsStateWithLifecycle()

    val drawerExtendedState by navigationManager.drawerExpandedState.collectAsStateWithLifecycle()
    val overlayTab by navigationManager.overlayTab.collectAsStateWithLifecycle()
    val selectedTab by navigationManager.selectedTab.collectAsStateWithLifecycle()

    val useServiceNavIcons by preferencesStore.useServiceNavLogos.collectAsStateWithLifecycle(false)
    val tabConfig by tabManager.tabConfiguration.collectAsStateWithLifecycle()
    val visibleTabs = tabConfig.visibleTabs
    val drawerTabs = tabConfig.drawerTabs

    val pagerState = rememberPagerState(
        initialPage = remember(visibleTabs, selectedTab) { 
            visibleTabs.indexOf(selectedTab).coerceAtLeast(0) 
        }
    ) { visibleTabs.size }

    LaunchedEffect(visibleTabs, overlayTab, tabConfig.isInitialValue) {
        if (tabConfig.isInitialValue) return@LaunchedEffect

        if (overlayTab == null) {
            if (selectedTab == null || selectedTab !in visibleTabs) {
                visibleTabs.firstOrNull()?.let {
                    navigationManager.setSelectedTab(it)
                }
            }
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

    CompositionLocalProvider(LocalNavigationManager provides navigationManager) {
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

        val mainContent = @Composable {
            AnimatedContent(
                targetState = overlayTab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                },
                label = "OverlayTransition"
            ) { currentOverlay ->
                if (currentOverlay != null) {
                    TabItemContent(currentOverlay, windowSizeClass, false)
                } else {
                    key(visibleTabs.isNotEmpty()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled = false,
                            beyondViewportPageCount = visibleTabs.size,
                            key = { page -> visibleTabs[page].key }
                        ) { page ->
                            TabItemContent(visibleTabs[page], windowSizeClass, isExpanded)
                        }
                    }
                }
            }
        }

        ModalNavigationDrawer(
            gesturesEnabled = overlayTab !is TabItem.CustomWebpage && windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact,
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
            }
        ) {
            if (isExpanded) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (overlayTab == null) {
                        NavigationRail(
                            header = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    IconButton(
                                        onClick = { navigationManager.openDrawer() }
                                    ) {
                                        Icon(Icons.Default.Menu, contentDescription = null)
                                    }

                                    val currentTab = overlayTab ?: selectedTab
                                    val associatedType = currentTab?.associatedType
                                    val navigator =
                                        associatedType?.takeIf { it in InstanceType.arrs() }
                                            ?.let { navigationManager.arr(it) }

                                    val hasInstances = associatedType?.let { type ->
                                        allInstances.any { it.type == type }
                                    } ?: false

                                    if (hasInstances && navigator?.backStack?.lastOrNull() is ArrScreen.Library) {
                                        FloatingActionButton(
                                            onClick = { navigator.toSearch() }
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                        }
                                    }
                                }
                            }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                visibleTabs.forEach { entry ->
                                    NavigationRailItem(
                                        selected = entry == selectedTab,
                                        onClick = { navigationManager.setSelectedTab(entry) },
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
                                                    Icon(
                                                        Icons.Default.Language,
                                                        contentDescription = entry.name
                                                    )
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
                    }
                    mainContent()
                }
            } else {
                NavigationSuiteScaffold(
                    layoutType = if (overlayTab != null) {
                        NavigationSuiteType.None
                    } else {
                        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
                    },
                    navigationSuiteItems = {
                        if (overlayTab == null) {
                            visibleTabs.forEach { entry ->
                                item(
                                    selected = entry == selectedTab,
                                    onClick = { navigationManager.setSelectedTab(entry) },
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
                                                Icon(
                                                    Icons.Default.Language,
                                                    contentDescription = entry.name
                                                )
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
                ) {
                    mainContent()
                }
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
private fun TabItemContent(tab: TabItem, windowSizeClass: WindowSizeClass, wideRailIsVisible: Boolean) {
    when (tab) {
        is TabItem.Standard -> {
            StandardTabContent(tab, windowSizeClass, wideRailIsVisible)
        }
        is TabItem.CustomWebpage -> {
            key(tab.id) {
                CustomWebpageViewerScreen(webpageId = tab.id, wideRailIsVisible = wideRailIsVisible)
            }
        }
        is TabItem.Settings -> SettingsTabNavHost(windowSizeClass)
    }
}

@Composable
private fun StandardTabContent(tab: TabItem.Standard, windowSizeClass: WindowSizeClass, wideRailIsVisible: Boolean) {
    when (tab) {
        TabItem.Standard.SHOWS -> ArrTab(InstanceType.Sonarr, windowSizeClass, wideRailIsVisible)
        TabItem.Standard.MOVIES -> ArrTab(InstanceType.Radarr, windowSizeClass, wideRailIsVisible)
        TabItem.Standard.MUSIC -> ArrTab(InstanceType.Lidarr, windowSizeClass, wideRailIsVisible)
        TabItem.Standard.BOOKS -> ArrTab(InstanceType.Booksehelf, windowSizeClass, wideRailIsVisible)
        TabItem.Standard.AUDIOBOOKS -> ArrTab(InstanceType.Listenarr, windowSizeClass, wideRailIsVisible)
        TabItem.Standard.ACTIVITY -> ActivityTab(wideRailIsVisible)
        TabItem.Standard.DOWNLOADS -> DownloadsTab(wideRailIsVisible)
        TabItem.Standard.CALENDAR -> CalendarTab(windowSizeClass, wideRailIsVisible)
        TabItem.Standard.REQUESTS -> SeerrTab(windowSizeClass, wideRailIsVisible)
        TabItem.Standard.PROWLARR -> ProwlarrTab(wideRailIsVisible)
        TabItem.Standard.DASHBOARD -> DashboardTab(windowSizeClass)
        TabItem.Standard.BAZARR -> BazarrTab(windowSizeClass, wideRailIsVisible)
    }
}
