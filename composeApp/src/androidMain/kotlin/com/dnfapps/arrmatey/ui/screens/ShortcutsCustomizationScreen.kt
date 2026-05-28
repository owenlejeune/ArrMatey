package com.dnfapps.arrmatey.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.datastore.AndroidPreferencesStore
import com.dnfapps.arrmatey.navigation.settingsNavigator
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shortcuts.AppShortcutManager
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortcutsCustomizationScreen(
    preferenceStore: AndroidPreferencesStore = koinInject(),
    shortcutManager: AppShortcutManager = koinInject()
) {
    val navigation = settingsNavigator
    val scope = rememberCoroutineScope()
    
    var shortcutItems by remember { mutableStateOf<List<AppShortcutManager.ShortcutItem>>(emptyList()) }
    val savedOrder by preferenceStore.shortcutsOrder.collectAsState(initial = emptyList())
    val disabledShortcuts by preferenceStore.disabledShortcuts.collectAsState(initial = emptySet())

    LaunchedEffect(Unit) {
        val available = shortcutManager.getAllAvailableShortcuts()
        val shortcutMap = available.associateBy { it.id }
        
        val ordered = if (savedOrder.isEmpty()) {
            available
        } else {
            val existingOrder = savedOrder.mapNotNull { shortcutMap[it] }
            val newOnes = available.filter { it.id !in savedOrder }
            existingOrder + newOnes
        }

        shortcutItems = ordered.sortedBy { it.id in disabledShortcuts }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.customize_shortcuts)) },
                navigationIcon = { BackButton(navigation) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ShortcutsList(
                items = shortcutItems,
                disabledIds = disabledShortcuts,
                onMove = { fromIndex, toIndex ->
                    val newList = shortcutItems.toMutableList()
                    val movedItem = newList.removeAt(fromIndex)
                    newList.add(toIndex, movedItem)
                    
                    val firstDisabledIndex = shortcutItems.indexOfFirst { it.id in disabledShortcuts }
                    val n = if (firstDisabledIndex == -1) shortcutItems.size else firstDisabledIndex
                    
                    var nextDisabled = disabledShortcuts
                    if (n in (toIndex + 1)..fromIndex) {
                        nextDisabled = nextDisabled - movedItem.id
                    } else if (n in (fromIndex + 1)..toIndex) {
                        nextDisabled = nextDisabled + movedItem.id
                    }
                    
                    shortcutItems = newList
                    scope.launch {
                        preferenceStore.saveDisabledShortcuts(nextDisabled)
                        preferenceStore.saveShortcutsOrder(newList.map { it.id })
                        shortcutManager.updateShortcuts()
                    }
                },
                onToggle = { id, enabled ->
                    scope.launch {
                        val currentDisabled = preferenceStore.disabledShortcuts.first()
                        val nextDisabled = if (enabled) currentDisabled - id else currentDisabled + id
                        preferenceStore.saveDisabledShortcuts(nextDisabled)
                        
                        // Re-sort items: enabled ones at top, disabled at bottom, maintaining relative order
                        val newList = shortcutItems.toMutableList()
                        val itemIndex = newList.indexOfFirst { it.id == id }
                        if (itemIndex != -1) {
                            val item = newList.removeAt(itemIndex)
                            if (enabled) {
                                // Find the first disabled item in the current state to insert before it
                                val firstDisabledIdx = newList.indexOfFirst { it.id in nextDisabled }
                                if (firstDisabledIdx == -1) {
                                    newList.add(item)
                                } else {
                                    newList.add(firstDisabledIdx, item)
                                }
                            } else {
                                // Move to the end of the list
                                newList.add(item)
                            }
                        }
                        shortcutItems = newList
                        preferenceStore.saveShortcutsOrder(newList.map { it.id })
                        shortcutManager.updateShortcuts()
                    }
                }
            )
        }
    }
}

@Composable
fun ShortcutsList(
    items: List<AppShortcutManager.ShortcutItem>,
    disabledIds: Set<String>,
    onMove: (Int, Int) -> Unit,
    onToggle: (String, Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()

    val reorderableLazyColumnState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            val fromIndex = items.indexOfFirst { it.id == from.key }
            val toIndex = items.indexOfFirst { it.id == to.key }

            if (fromIndex != -1 && toIndex != -1 && fromIndex != toIndex) {
                onMove(fromIndex, toIndex)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    )

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ContainerCard(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = mokoString(MR.strings.customize_shortcuts_description),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(items, key = { it.id }) { item ->
            ReorderableItem(reorderableLazyColumnState, item.id) { isDragging ->
                val interactionSource = remember { MutableInteractionSource() }
                val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                val isEnabled = item.id !in disabledIds
                val alpha by animateFloatAsState(if (isEnabled) 1f else 0.6f)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .graphicsLayer { this.alpha = alpha },
                    elevation = CardDefaults.cardElevation(defaultElevation = elevation)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = null,
                            modifier = Modifier.draggableHandle(
                                enabled = true,
                                interactionSource = interactionSource
                            )
                        )

                        Icon(
                            painter = painterResource(item.iconRes),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        Text(
                            text = item.label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { onToggle(item.id, it) }
                        )
                    }
                }
            }
        }
    }
}
