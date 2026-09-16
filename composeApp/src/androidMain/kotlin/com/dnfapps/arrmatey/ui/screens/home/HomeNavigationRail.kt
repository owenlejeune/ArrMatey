package com.dnfapps.arrmatey.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.toSearch

@Composable
fun HomeNavigationRail(
    visibleTabs: List<TabItem>,
    selectedTab: TabItem?,
    overlayTab: TabItem?,
    allInstances: List<Instance>,
    navigationManager: NavigationManager,
    useServiceNavIcons: Boolean,
    activityQueueIssuesCount: Int,
    onOpenDrawer: () -> Unit,
    onSelectTab: (TabItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(
        modifier = modifier,
        header = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Default.Menu, contentDescription = null)
                }

                val currentTab = overlayTab ?: selectedTab
                val isLibrary = currentTab == TabItem.Standard.LIBRARY
                val hasInstances =
                    if (isLibrary) {
                        allInstances.any { it.type in InstanceType.arrs() }
                    } else {
                        currentTab?.associatedType?.let { type -> allInstances.any { it.type == type } } == true
                    }
                val navigator = navigationManager.getNavigator(currentTab)

                Box(modifier = Modifier.size(56.dp)) {
                    if (hasInstances && navigator?.backStack?.lastOrNull() is ArrScreen.Library) {
                        FloatingActionButton(
                            onClick = { navigator.toSearch(type = currentTab?.associatedType) },
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            visibleTabs.forEach { entry ->
                NavigationRailItem(
                    selected = entry == selectedTab,
                    onClick = { onSelectTab(entry) },
                    icon = {
                        HomeTabNavIcon(
                            tabItem = entry,
                            useServiceNavIcons = useServiceNavIcons,
                            activityQueueIssuesCount = activityQueueIssuesCount,
                        )
                    },
                    label = { HomeTabNavLabel(entry) },
                )
            }
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}
