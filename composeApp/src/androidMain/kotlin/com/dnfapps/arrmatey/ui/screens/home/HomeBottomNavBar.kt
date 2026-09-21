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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeBottomNavBar(
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

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = NavigationBarDefaults.Elevation,
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                    onLongClick = handleLongPress,
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                    .height(80.dp)
                    .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            visibleTabs.forEach { entry ->
                HomeBottomNavItem(
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
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeBottomNavItem(
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
        label = "BottomNavIndicatorColor",
    )

    val iconColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = animationSpec,
        label = "BottomNavIconColor",
    )

    val textColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = animationSpec,
        label = "BottomNavTextColor",
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
                ).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 64.dp, height = 32.dp)
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
