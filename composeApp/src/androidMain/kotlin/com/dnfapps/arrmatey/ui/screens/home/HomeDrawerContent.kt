package com.dnfapps.arrmatey.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun HomeDrawerContent(
    tabManager: TabManager,
    tabConfig: TabManager.TabConfiguration,
    drawerTabs: List<TabItem>,
    overlayTab: TabItem?,
    useServiceNavIcons: Boolean,
    activityQueueIssuesCount: Int,
    onHomeClick: () -> Unit,
    onDrawerTabClick: (TabItem) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showHiddenSection by remember { mutableStateOf(false) }
    var tabToHide by remember { mutableStateOf<TabItem?>(null) }

    LaunchedEffect(showHiddenSection) {
        if (showHiddenSection) {
            delay(10.seconds)
            showHiddenSection = false
        }
    }

    tabToHide?.let { tab ->
        HideTabConfirmationDialog(
            onConfirm = {
                tabManager.hideTab(tab)
                tabToHide = null
            },
            onDismiss = { tabToHide = null },
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showHiddenSection = true
                        },
                    )
                }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        NavigationDrawerItem(
            label = { Text(mokoString(MR.strings.home)) },
            selected = overlayTab == null,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            onClick = onHomeClick,
        )
        HorizontalDivider()

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            drawerTabs.forEach { item ->
                NavigationDrawerItem(
                    label = { HomeTabNavLabel(item) },
                    selected = overlayTab == item && !isEditMode,
                    icon = {
                        HomeTabNavIcon(
                            tabItem = item,
                            useServiceNavIcons = useServiceNavIcons,
                            activityQueueIssuesCount = activityQueueIssuesCount,
                        )
                    },
                    badge = {
                        AnimatedVisibility(visible = isEditMode) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                    onClick = {
                        if (isEditMode) {
                            tabToHide = item
                        } else {
                            onDrawerTabClick(item)
                        }
                    },
                )
            }

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = showHiddenSection && tabConfig.hiddenTabs.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                DrawerHiddenTabsSection(
                    hiddenTabs = tabConfig.hiddenTabs,
                    isEditMode = isEditMode,
                    useServiceNavIcons = useServiceNavIcons,
                    activityQueueIssuesCount = activityQueueIssuesCount,
                    onTabClick = { item ->
                        if (isEditMode) {
                            tabManager.restoreTab(item)
                        } else {
                            onDrawerTabClick(item)
                        }
                        showHiddenSection = false
                    },
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            NavigationDrawerItem(
                selected = overlayTab == TabItem.Settings,
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text(mokoString(MR.strings.settings)) },
                onClick = onSettingsClick,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { isEditMode = !isEditMode },
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = isEditMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.92f))
                            .togetherWith(
                                fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.92f),
                            )
                    },
                    label = "IconTransition",
                ) { editing ->
                    Icon(
                        imageVector = if (editing) Icons.Default.Close else Icons.Default.Edit,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerHiddenTabsSection(
    hiddenTabs: List<TabItem>,
    isEditMode: Boolean,
    useServiceNavIcons: Boolean,
    activityQueueIssuesCount: Int,
    onTabClick: (TabItem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = mokoString(MR.strings.navigation_items_hidden),
            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
        )
        hiddenTabs.forEach { item ->
            NavigationDrawerItem(
                label = { HomeTabNavLabel(item) },
                selected = false,
                icon = {
                    HomeTabNavIcon(
                        tabItem = item,
                        useServiceNavIcons = useServiceNavIcons,
                        activityQueueIssuesCount = activityQueueIssuesCount,
                    )
                },
                onClick = { onTabClick(item) },
            )
        }
    }
}

@Composable
private fun HideTabConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(mokoString(MR.strings.remove)) },
        text = { Text(mokoString(MR.strings.remove_navigation_tab_confirm)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(mokoString(MR.strings.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(mokoString(MR.strings.cancel))
            }
        },
    )
}
