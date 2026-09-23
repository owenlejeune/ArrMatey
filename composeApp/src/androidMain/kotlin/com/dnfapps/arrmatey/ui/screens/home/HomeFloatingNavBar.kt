package com.dnfapps.arrmatey.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
    onLongPressTab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val handleLongPress = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onLongPressTab()
    }

    FloatingNavigationBar(
        onLongClick = handleLongPress,
        modifier = modifier.padding(bottom = navigationBarBottomInset())
    ) {
        visibleTabs.forEach { entry ->
            FloatingNavigationBarItem(
                selected = entry == selectedTab,
                onClick = { onSelectTab(entry) },
                onLongClick = handleLongPress,
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
