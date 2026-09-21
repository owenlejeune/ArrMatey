package com.dnfapps.arrmatey.ui.screens.onboarding

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.datastore.TabPreferences
import com.dnfapps.arrmatey.entensions.androidIcon
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.appbar.FloatingNavigationBar
import com.dnfapps.arrmatey.ui.components.appbar.FloatingNavigationBarItem
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationSetupPage(
    useFloatingNavigationBar: Boolean,
    onToggleFloatingNavigationBar: () -> Unit,
    tabPreferences: TabPreferences,
    onUpdateTabPreferences: (TabPreferences) -> Unit,
    useServiceNavLogos: Boolean,
    onToggleServiceNavLogos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showReorderSheet by remember { mutableStateOf(false) }

    val standardTabs = remember { TabItem.Standard.entries.filter { !it.isDisabled } }
    val standardMap = remember { standardTabs.associateBy { it.key } }

    val visibleTabs =
        remember(tabPreferences.orderedVisibleKeys) {
            tabPreferences.orderedVisibleKeys.mapNotNull { standardMap[it] }
        }

    fun moveTab(
        fromIndex: Int,
        toIndex: Int,
    ) {
        if (fromIndex in visibleTabs.indices && toIndex in visibleTabs.indices) {
            val newVisible = tabPreferences.orderedVisibleKeys.toMutableList()
            val item = newVisible.removeAt(fromIndex)
            newVisible.add(toIndex, item)
            onUpdateTabPreferences(tabPreferences.copy(orderedVisibleKeys = newVisible))
        }
    }

    fun removeTab(tab: TabItem) {
        if (visibleTabs.size > 1) {
            val newVisible = tabPreferences.orderedVisibleKeys - tab.key
            val newHidden = tabPreferences.orderedHiddenKeys + tab.key
            onUpdateTabPreferences(tabPreferences.copy(orderedVisibleKeys = newVisible, orderedHiddenKeys = newHidden))
        }
    }

    fun addTab(tab: TabItem) {
        if (visibleTabs.size < 5) {
            val newVisible = tabPreferences.orderedVisibleKeys + tab.key
            val newHidden = tabPreferences.orderedHiddenKeys - tab.key
            val newRemoved = tabPreferences.orderedRemovedKeys - tab.key
            onUpdateTabPreferences(TabPreferences(newVisible, newHidden, newRemoved))
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = mokoString(MR.strings.onboarding_pref_nav_tabs_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = mokoString(MR.strings.onboarding_pref_nav_tabs_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PreferenceToggleCard(
                modifier = Modifier.padding(horizontal = 24.dp),
                icon = Icons.Default.Tune,
                title = mokoString(MR.strings.onboarding_pref_nav_logos),
                description = mokoString(MR.strings.onboarding_pref_nav_logos_desc),
                checked = useServiceNavLogos,
                onCheckedChange = { onToggleServiceNavLogos() },
            )

            // Floating vs Standard Selector Card
            Card(
                modifier = Modifier.padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = mokoString(MR.strings.floating_navigation_bar_toggle_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val options =
                            listOf(
                                true to mokoString(MR.strings.onboarding_pref_nav_style_floating),
                                false to mokoString(MR.strings.onboarding_pref_nav_style_standard),
                            )
                        options.forEach { (isFloating, label) ->
                            val isSelected = useFloatingNavigationBar == isFloating
                            Surface(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .clip(MaterialTheme.shapes.medium)
                                        .clickable {
                                            if (useFloatingNavigationBar != isFloating) {
                                                onToggleFloatingNavigationBar()
                                            }
                                        }.border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color =
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.outlineVariant
                                                },
                                            shape = MaterialTheme.shapes.medium,
                                        ),
                                color =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Navigation Tabs Card (FlowRow Chips)
            Card(
                modifier = Modifier.padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Header with Selected Count & Reorder Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = mokoString(MR.strings.navigation_items_selected),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "${visibleTabs.size} / 5",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        if (visibleTabs.size > 1) {
                            TextButton(
                                onClick = { showReorderSheet = true },
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = mokoString(MR.strings.onboarding_pref_nav_reorder_button),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }

                    // FlowRow of All Standard Tabs (Tap to Add / Remove)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        standardTabs.forEach { tab ->
                            val isSelected = tab.key in tabPreferences.orderedVisibleKeys
                            val canSelect = isSelected || visibleTabs.size < 5

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        removeTab(tab)
                                    } else {
                                        addTab(tab)
                                    }
                                },
                                enabled = canSelect || isSelected,
                                leadingIcon = {
                                    val logo = tab.associatedType?.tabIcon
                                    if (useServiceNavLogos && logo != null) {
                                        Icon(
                                            painter = painterResource(logo),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    } else {
                                        Icon(
                                            imageVector = tab.androidIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = mokoString(tab.resource),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        val position = visibleTabs.indexOf(tab) + 1
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(18.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primary,
                                                        CircleShape,
                                                    ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = "$position",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

        // Live Navigation Bar Preview (OUTSIDE OF CARD, NO HORIZONTAL PADDING)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (useFloatingNavigationBar) {
                FloatingNavigationBar {
                    visibleTabs.forEachIndexed { index, tab ->
                        FloatingNavigationBarItem(
                            selected = index == 0,
                            onClick = {},
                            icon = {
                                val logo = tab.associatedType?.tabIcon
                                if (useServiceNavLogos && logo != null) {
                                    Icon(
                                        painter = painterResource(logo),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                } else {
                                    Icon(
                                        imageVector = tab.androidIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            },
                            label = {
                                Text(mokoString(tab.resource))
                            },
                        )
                    }
                }
            } else {
                NavigationBar {
                    visibleTabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = index == 0,
                            onClick = {},
                            icon = {
                                val logo = tab.associatedType?.tabIcon
                                if (useServiceNavLogos && logo != null) {
                                    Icon(
                                        painter = painterResource(logo),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                } else {
                                    Icon(
                                        imageVector = tab.androidIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = mokoString(tab.resource),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    if (showReorderSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReorderSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = mokoString(MR.strings.onboarding_pref_nav_reorder_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = mokoString(MR.strings.onboarding_pref_nav_reorder_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(visibleTabs, key = { _, tab -> tab.key }) { index, tab ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(24.dp)
                                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        )
                                    }

                                    val logo = tab.associatedType?.tabIcon
                                    if (useServiceNavLogos && logo != null) {
                                        Icon(
                                            painter = painterResource(logo),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    } else {
                                        Icon(
                                            imageVector = tab.androidIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }

                                    Text(
                                        text = mokoString(tab.resource),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                moveTab(index, index - 1)
                                            }
                                        },
                                        enabled = index > 0,
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                                    }

                                    IconButton(
                                        onClick = {
                                            if (index < visibleTabs.lastIndex) {
                                                moveTab(index, index + 1)
                                            }
                                        },
                                        enabled = index < visibleTabs.lastIndex,
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { showReorderSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(mokoString(MR.strings.ok))
                }
            }
        }
    }
}

@Composable
private fun PreferenceToggleCard(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}
