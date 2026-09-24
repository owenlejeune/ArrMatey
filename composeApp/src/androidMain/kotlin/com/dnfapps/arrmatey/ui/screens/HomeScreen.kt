package com.dnfapps.arrmatey.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.extensions.pxToDp
import com.dnfapps.arrmatey.navigation.LocalNavigationManager
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.ui.components.appbar.FloatingBarActionState
import com.dnfapps.arrmatey.ui.components.appbar.LocalFloatingBarActionState
import com.dnfapps.arrmatey.ui.components.navigation.DoubleBackToExit
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.screens.home.HomeBottomNavBar
import com.dnfapps.arrmatey.ui.screens.home.HomeDrawerContent
import com.dnfapps.arrmatey.ui.screens.home.HomeFloatingNavBar
import com.dnfapps.arrmatey.ui.screens.home.HomeMainContent
import com.dnfapps.arrmatey.ui.screens.home.HomeNavigationRail
import com.dnfapps.arrmatey.ui.sheets.TabCustomizationSheet
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    navigationManager: NavigationManager = koinInject(),
    preferencesStore: PreferencesStore = koinInject(),
    activityQueue: ActivityQueueViewModel = koinViewModel(),
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
    val useFloatingNavigationBar by preferencesStore.useFloatingNavigationBar.collectAsStateWithLifecycle(false)
    val tabConfig by tabManager.tabConfiguration.collectAsStateWithLifecycle()
    if (tabConfig.isInitialValue) return

    val visibleTabs = tabConfig.visibleTabs
    val drawerTabs = tabConfig.drawerTabs

    val currentSelectedTab = selectedTab ?: visibleTabs.firstOrNull()
    val pagerState =
        rememberPagerState(
            initialPage =
                remember(visibleTabs, currentSelectedTab) {
                    visibleTabs.indexOf(currentSelectedTab).coerceAtLeast(0)
                },
        ) { visibleTabs.size }

    LaunchedEffect(visibleTabs, overlayTab) {
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

    DoubleBackToExit(openDrawerInstead = overlayTab != null)

    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val floatingBarIsVisible = !isExpanded && useFloatingNavigationBar && overlayTab == null && visibleTabs.size > 1
    var floatingBarHeight by remember { mutableIntStateOf(0) }
    val floatingBarBottomPadding =
        if (floatingBarIsVisible) {
            floatingBarHeight.pxToDp()
        } else {
            0.dp
        }

    val floatingBarActionState = remember { FloatingBarActionState() }

    var showReorderSheet by remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalNavigationManager provides navigationManager,
        LocalFloatingBarBottomPadding provides floatingBarBottomPadding,
        LocalFloatingBarActionState provides floatingBarActionState,
    ) {
        val mainContent = @Composable {
            HomeMainContent(
                overlayTab = overlayTab,
                visibleTabs = visibleTabs,
                pagerState = pagerState,
                windowSizeClass = windowSizeClass,
                isExpanded = isExpanded,
            )
        }

        ModalNavigationDrawer(
            gesturesEnabled =
                overlayTab !is TabItem.CustomWebpage &&
                    !drawerState.isAnimationRunning,
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerState = drawerState) {
                    HomeDrawerContent(
                        tabManager = tabManager,
                        tabConfig = tabConfig,
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
                        },
                    )
                }
            },
        ) {
            if (isExpanded) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (overlayTab == null && visibleTabs.size > 1) {
                        HomeNavigationRail(
                            visibleTabs = visibleTabs,
                            selectedTab = selectedTab,
                            overlayTab = overlayTab,
                            allInstances = allInstances,
                            navigationManager = navigationManager,
                            useServiceNavIcons = useServiceNavIcons,
                            activityQueueIssuesCount = activityQueueIssuesCount,
                            onOpenDrawer = { navigationManager.openDrawer() },
                            onSelectTab = { navigationManager.setSelectedTab(it) },
                            onLongPressTab = { showReorderSheet = true },
                        )
                    }
                    mainContent()
                }
            } else if (useFloatingNavigationBar) {
                Box(modifier = Modifier.fillMaxSize()) {
                    mainContent()
                    if (overlayTab == null && visibleTabs.size > 1) {
                        HomeFloatingNavBar(
                            visibleTabs = visibleTabs,
                            selectedTab = selectedTab,
                            useServiceNavIcons = useServiceNavIcons,
                            activityQueueIssuesCount = activityQueueIssuesCount,
                            onSelectTab = { navigationManager.setSelectedTab(it) },
                            onLongPressTab = { showReorderSheet = true },
                            modifier =
                                Modifier.align(Alignment.BottomCenter).onGloballyPositioned {
                                    floatingBarHeight = it.size.height
                                },
                        )
                    }
                }
            } else {
                Scaffold(
                    contentWindowInsets = WindowInsets(0.dp),
                    bottomBar = {
                        if (overlayTab == null && visibleTabs.size > 1) {
                            HomeBottomNavBar(
                                visibleTabs = visibleTabs,
                                selectedTab = selectedTab,
                                useServiceNavIcons = useServiceNavIcons,
                                activityQueueIssuesCount = activityQueueIssuesCount,
                                onSelectTab = { navigationManager.setSelectedTab(it) },
                                onLongPressTab = { showReorderSheet = true },
                            )
                        }
                    },
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        mainContent()
                    }
                }
            }
        }

        if (showReorderSheet) {
            TabCustomizationSheet(
                onDismissRequest = { showReorderSheet = false },
            )
        }
    }
}
