package com.dnfapps.arrmatey.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.toSearch

@OptIn(ExperimentalFoundationApi::class)
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
    onLongPressTab: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val handleLongPress = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onLongPressTab()
    }
    NavigationRail(
        modifier =
            modifier.combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = handleLongPress,
            ),
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
                HomeNavigationRailItem(
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
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animationSpec = tween<Color>(durationMillis = 200, easing = FastOutSlowInEasing)

    val indicatorColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                Color.Transparent
            },
        animationSpec = animationSpec,
        label = "RailNavIndicatorColor",
    )

    val iconColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = animationSpec,
        label = "RailNavIconColor",
    )

    val textColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = animationSpec,
        label = "RailNavTextColor",
    )

    Column(
        modifier =
            modifier
                .semantics {
                    this.selected = selected
                    this.role = Role.Tab
                }.clip(CircleShape)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    onClick = onClick,
                    onLongClick = onLongClick,
                ).padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 56.dp, height = 32.dp)
                    .clip(CircleShape)
                    .background(indicatorColor),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides iconColor) {
                icon()
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        CompositionLocalProvider(LocalContentColor provides textColor) {
            ProvideTextStyle(
                value =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    ),
            ) {
                label()
            }
        }
    }
}
