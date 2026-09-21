package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.datastore.AndroidPreferencesStore
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shortcuts.AppShortcutManager
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutsCustomizationScreen(
    preferenceStore: AndroidPreferencesStore = koinInject(),
    shortcutManager: AppShortcutManager = koinInject(),
    onBack: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    var shortcutItems by remember { mutableStateOf<List<AppShortcutManager.ShortcutItem>>(emptyList()) }
    val savedOrder by preferenceStore.shortcutsOrder.collectAsState(initial = emptyList())
    val disabledShortcuts by preferenceStore.disabledShortcuts.collectAsState(initial = emptySet())

    LaunchedEffect(Unit) {
        val available = shortcutManager.getAllAvailableShortcuts()
        val shortcutMap = available.associateBy { it.id }

        val ordered =
            if (savedOrder.isEmpty()) {
                available
            } else {
                val existingOrder = savedOrder.mapNotNull { shortcutMap[it] }
                val newOnes = available.filter { it.id !in savedOrder }
                existingOrder + newOnes
            }

        shortcutItems = ordered
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.customize_shortcuts)) },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            val enabledShortcuts =
                remember(shortcutItems, disabledShortcuts) {
                    shortcutItems.filter { it.id !in disabledShortcuts }
                }
            val disabledShortcutItems =
                remember(shortcutItems, disabledShortcuts) {
                    shortcutItems.filter { it.id in disabledShortcuts }
                }

            ShortcutsList(
                enabledItems = enabledShortcuts,
                disabledItems = disabledShortcutItems,
                onMoveEnabled = { fromIndex, toIndex ->
                    val newEnabled = enabledShortcuts.toMutableList()
                    val item = newEnabled.removeAt(fromIndex)
                    newEnabled.add(toIndex, item)

                    val newFullList = newEnabled + disabledShortcutItems
                    shortcutItems = newFullList
                    scope.launch {
                        preferenceStore.saveShortcutsOrder(newFullList.map { it.id })
                        shortcutManager.updateShortcuts()
                    }
                },
                onDisable = { item ->
                    scope.launch {
                        val currentDisabled = preferenceStore.disabledShortcuts.first()
                        val nextDisabled = currentDisabled + item.id
                        val newEnabled = enabledShortcuts.filter { it.id != item.id }
                        val newDisabled = disabledShortcutItems + item
                        val newFullList = newEnabled + newDisabled
                        shortcutItems = newFullList
                        preferenceStore.saveDisabledShortcuts(nextDisabled)
                        preferenceStore.saveShortcutsOrder(newFullList.map { it.id })
                        shortcutManager.updateShortcuts()
                    }
                },
                onEnable = { item ->
                    scope.launch {
                        val currentDisabled = preferenceStore.disabledShortcuts.first()
                        val nextDisabled = currentDisabled - item.id
                        val newDisabled = disabledShortcutItems.filter { it.id != item.id }
                        val newEnabled = enabledShortcuts + item
                        val newFullList = newEnabled + newDisabled
                        shortcutItems = newFullList
                        preferenceStore.saveDisabledShortcuts(nextDisabled)
                        preferenceStore.saveShortcutsOrder(newFullList.map { it.id })
                        shortcutManager.updateShortcuts()
                    }
                },
            )
        }
    }
}

@Composable
fun ShortcutsList(
    enabledItems: List<AppShortcutManager.ShortcutItem>,
    disabledItems: List<AppShortcutManager.ShortcutItem>,
    onMoveEnabled: (Int, Int) -> Unit,
    onDisable: (AppShortcutManager.ShortcutItem) -> Unit,
    onEnable: (AppShortcutManager.ShortcutItem) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 16.dp + LocalFloatingBarBottomPadding.current),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header_description") {
            ContainerCard(
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Text(
                    text = mokoString(MR.strings.customize_shortcuts_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (enabledItems.isNotEmpty()) {
            item(key = "header_enabled") {
                Text(
                    text = mokoString(MR.strings.navigation_items_visible),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.animateItem(),
                )
            }

            itemsIndexed(
                items = enabledItems,
                key = { _, item -> item.id },
            ) { index, item ->
                ContainerCard(
                    modifier = Modifier.animateItem(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                painter = painterResource(item.iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )

                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { onMoveEnabled(index, index - 1) },
                                enabled = index > 0,
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                            }

                            IconButton(
                                onClick = { onMoveEnabled(index, index + 1) },
                                enabled = index < enabledItems.lastIndex,
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                            }

                            IconButton(
                                onClick = { onDisable(item) },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Disable Shortcut",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (disabledItems.isNotEmpty()) {
            item(key = "header_disabled") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mokoString(MR.strings.navigation_items_hidden),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.animateItem(),
                )
            }

            itemsIndexed(
                items = disabledItems,
                key = { _, item -> item.id },
            ) { _, item ->
                ContainerCard(
                    modifier = Modifier.animateItem(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                painter = painterResource(item.iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )

                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }

                        IconButton(
                            onClick = { onEnable(item) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Enable Shortcut",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}
