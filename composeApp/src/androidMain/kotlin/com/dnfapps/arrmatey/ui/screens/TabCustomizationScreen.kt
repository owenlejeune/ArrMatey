package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.datastore.TabPreferences
import com.dnfapps.arrmatey.entensions.androidIcon
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject

private const val MAX_TABS = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabCustomizationScreen(
    preferenceStore: PreferencesStore = koinInject(),
    tabManager: TabManager = koinInject(),
    onBack: () -> Unit = {},
) {
    val tabConfig by tabManager.tabConfiguration.collectAsStateWithLifecycle()
    val useServiceNavLogos by preferenceStore.useServiceNavLogos.collectAsStateWithLifecycle(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.customize_navigation)) },
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    IconButton(onClick = { preferenceStore.resetTabPreferences() }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabCustomizationContent(
                useServiceNavLogos = useServiceNavLogos,
                visibleTabs = tabConfig.visibleTabs,
                drawerTabs = tabConfig.drawerTabs,
                hiddenTabs = tabConfig.hiddenTabs,
                updatePreferences = { preferenceStore.updateTabPreferences(it) },
            )
        }
    }
}

@Composable
fun TabCustomizationContent(
    useServiceNavLogos: Boolean,
    visibleTabs: List<TabItem>,
    drawerTabs: List<TabItem>,
    hiddenTabs: List<TabItem>,
    updatePreferences: (TabPreferences) -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
    contentPadding: PaddingValues = PaddingValues(bottom = 16.dp + LocalFloatingBarBottomPadding.current),
) {
    fun moveVisible(fromIndex: Int, toIndex: Int) {
        val newVisible = visibleTabs.toMutableList()
        val item = newVisible.removeAt(fromIndex)
        newVisible.add(toIndex, item)
        updatePreferences(
            TabPreferences(
                orderedVisibleKeys = newVisible.map { it.key },
                orderedHiddenKeys = drawerTabs.map { it.key },
                orderedRemovedKeys = hiddenTabs.map { it.key },
            ),
        )
    }

    fun moveToDrawer(tab: TabItem) {
        if (visibleTabs.size <= 1) return
        val newVisible = visibleTabs.filter { it.key != tab.key }
        val newDrawer = drawerTabs + tab
        updatePreferences(
            TabPreferences(
                orderedVisibleKeys = newVisible.map { it.key },
                orderedHiddenKeys = newDrawer.map { it.key },
                orderedRemovedKeys = hiddenTabs.map { it.key },
            ),
        )
    }

    fun moveDrawer(fromIndex: Int, toIndex: Int) {
        val newDrawer = drawerTabs.toMutableList()
        val item = newDrawer.removeAt(fromIndex)
        newDrawer.add(toIndex, item)
        updatePreferences(
            TabPreferences(
                orderedVisibleKeys = visibleTabs.map { it.key },
                orderedHiddenKeys = newDrawer.map { it.key },
                orderedRemovedKeys = hiddenTabs.map { it.key },
            ),
        )
    }

    fun promoteToVisible(tab: TabItem) {
        if (visibleTabs.size >= MAX_TABS) return
        val newDrawer = drawerTabs.filter { it.key != tab.key }
        val newVisible = visibleTabs + tab
        updatePreferences(
            TabPreferences(
                orderedVisibleKeys = newVisible.map { it.key },
                orderedHiddenKeys = newDrawer.map { it.key },
                orderedRemovedKeys = hiddenTabs.map { it.key },
            ),
        )
    }

    fun moveToHidden(tab: TabItem) {
        val newDrawer = drawerTabs.filter { it.key != tab.key }
        val newHidden = hiddenTabs + tab
        updatePreferences(
            TabPreferences(
                orderedVisibleKeys = visibleTabs.map { it.key },
                orderedHiddenKeys = newDrawer.map { it.key },
                orderedRemovedKeys = newHidden.map { it.key },
            ),
        )
    }

    fun restoreToDrawer(tab: TabItem) {
        val newHidden = hiddenTabs.filter { it.key != tab.key }
        val newDrawer = drawerTabs + tab
        updatePreferences(
            TabPreferences(
                orderedVisibleKeys = visibleTabs.map { it.key },
                orderedHiddenKeys = newDrawer.map { it.key },
                orderedRemovedKeys = hiddenTabs.map { it.key },
            ),
        )
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header_description") {
            ContainerCard(
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Text(
                    text = mokoString(MR.strings.customize_navigation_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (visibleTabs.isNotEmpty()) {
            item(key = "header_visible") {
                Text(
                    text = mokoString(MR.strings.navigation_items_selected),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.animateItem(),
                )
            }

            itemsIndexed(
                items = visibleTabs,
                key = { _, tab -> tab.key },
            ) { index, tab ->
                ContainerCard(
                    modifier = Modifier.animateItem(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            TabItemIconAndLabel(tab = tab, useServiceNavLogos = useServiceNavLogos)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { moveVisible(index, index - 1) },
                                enabled = index > 0,
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                            }

                            IconButton(
                                onClick = { moveVisible(index, index + 1) },
                                enabled = index < visibleTabs.lastIndex,
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                            }

                            IconButton(
                                onClick = { moveToDrawer(tab) },
                                enabled = visibleTabs.size > 1,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Move to Drawer",
                                    tint =
                                        if (visibleTabs.size > 1) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (drawerTabs.isNotEmpty()) {
            item(key = "header_drawer") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mokoString(MR.strings.navigation_items_drawer),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.animateItem(),
                )
            }

            itemsIndexed(
                items = drawerTabs,
                key = { _, tab -> tab.key },
            ) { index, tab ->
                ContainerCard(
                    modifier = Modifier.animateItem(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            TabItemIconAndLabel(tab = tab, useServiceNavLogos = useServiceNavLogos)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { moveDrawer(index, index - 1) },
                                enabled = index > 0,
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                            }

                            IconButton(
                                onClick = { moveDrawer(index, index + 1) },
                                enabled = index < drawerTabs.lastIndex,
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                            }

                            IconButton(
                                onClick = { promoteToVisible(tab) },
                                enabled = visibleTabs.size < MAX_TABS,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Promote to Nav Bar",
                                    tint =
                                        if (visibleTabs.size < MAX_TABS) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        },
                                )
                            }

                            IconButton(
                                onClick = { moveToHidden(tab) },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "Hide Tab",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (hiddenTabs.isNotEmpty()) {
            item(key = "header_hidden") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mokoString(MR.strings.navigation_items_hidden),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.animateItem(),
                )
            }

            itemsIndexed(
                items = hiddenTabs,
                key = { _, tab -> tab.key },
            ) { _, tab ->
                ContainerCard(
                    modifier = Modifier.animateItem(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            TabItemIconAndLabel(tab = tab, useServiceNavLogos = useServiceNavLogos)
                        }

                        IconButton(
                            onClick = { restoreToDrawer(tab) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Restore Tab",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabItemIconAndLabel(
    tab: TabItem,
    useServiceNavLogos: Boolean,
) {
    when (tab) {
        is TabItem.Standard -> {
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
                )
            }
            Text(
                text = mokoString(tab.resource),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        is TabItem.CustomWebpage -> {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
            )
            Column {
                Text(
                    text = tab.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = tab.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TabItem.Settings -> {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
            )
            Text(
                text = mokoString(tab.resource),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
