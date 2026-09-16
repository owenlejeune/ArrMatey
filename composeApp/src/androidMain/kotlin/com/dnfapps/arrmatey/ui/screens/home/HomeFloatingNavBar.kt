package com.dnfapps.arrmatey.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.ui.components.appbar.FloatingNavigationBar
import com.dnfapps.arrmatey.ui.components.appbar.FloatingNavigationBarItem
import com.dnfapps.arrmatey.utils.navigationBarBottomInset

@Composable
fun HomeFloatingNavBar(
    visibleTabs: List<TabItem>,
    selectedTab: TabItem?,
    useServiceNavIcons: Boolean,
    activityQueueIssuesCount: Int,
    onSelectTab: (TabItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingNavigationBar(
        modifier = modifier.padding(bottom = navigationBarBottomInset() + 16.dp),
    ) {
        visibleTabs.forEach { entry ->
            FloatingNavigationBarItem(
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
    }
}
