package com.dnfapps.arrmatey.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.compose.TabManager
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.datastore.TabPreferences
import com.dnfapps.arrmatey.entensions.androidIcon
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.webpage.model.CustomWebpage
import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private const val MAX_TABS = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabCustomizationScreen(
    preferenceStore: PreferencesStore = koinInject(),
    tabManager: TabManager = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SettingsScreen> = navigationManager.settings()
) {
    val tabConfig by tabManager.tabConfiguration.collectAsStateWithLifecycle()
    val useServiceNavLogos by preferenceStore.useServiceNavLogos.collectAsStateWithLifecycle(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.customize_navigation)) },
                navigationIcon = { BackButton(navigation) },
                actions = {
                    IconButton(onClick = { preferenceStore.resetTabPreferences() }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabCustomizationContent(
                useServiceNavLogos = useServiceNavLogos,
                visibleTabs = tabConfig.visibleTabs,
                drawerTabs = tabConfig.drawerTabs,
                updatePreferences = { preferenceStore.updateTabPreferences(it) }
            )
        }
    }
}

@Composable
fun TabCustomizationContent(
    useServiceNavLogos: Boolean,
    visibleTabs: List<TabItem>,
    drawerTabs: List<TabItem>,
    updatePreferences: (TabPreferences) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var combinedList by remember {
        mutableStateOf(TabRow.buildList(visibleTabs, drawerTabs))
    }

    LaunchedEffect(visibleTabs, drawerTabs) {
        combinedList = TabRow.buildList(visibleTabs, drawerTabs)
    }

    val lazyListState = rememberLazyListState()
    val reorderableLazyColumnState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            val fromIndex = combinedList.indexOfFirst { it.key == from.key }
            val toIndex = combinedList.indexOfFirst { it.key == to.key }

            if (fromIndex != -1 && toIndex != -1) {
                val newList = combinedList.toMutableList()

                val movedItem = newList.removeAt(fromIndex)
                newList.add(toIndex, movedItem)

                val dividerIndex = newList.indexOfFirst { it is TabRow.Divider }
                val tabsAbove = newList.subList(0, dividerIndex).filterIsInstance<TabRow.Tab>()
                if (tabsAbove.size > MAX_TABS) {
                    val overflowItem = newList.removeAt(dividerIndex - 1)
                    newList.add(dividerIndex, overflowItem)
                }

                combinedList = newList
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                val filtered = combinedList.filter { it !is TabRow.Placeholder }
                val finalDividerIndex = filtered.indexOfFirst { it is TabRow.Divider }

                val newNav = filtered.subList(0, finalDividerIndex)
                    .filterIsInstance<TabRow.Tab>()
                    .map { it.item.key }

                val newHidden = filtered.subList(finalDividerIndex + 1, filtered.size)
                    .filterIsInstance<TabRow.Tab>()
                    .map { it.item.key }

                updatePreferences(TabPreferences(newNav, newHidden))
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
        item(key = "header_static_section") {
            ContainerCard(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = mokoString(MR.strings.customize_navigation_description),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = mokoString(MR.strings.navigation_items_selected),
                style = MaterialTheme.typography.titleMedium
            )
        }

        itemsIndexed(combinedList, key = { _, item -> item.key }) { index, row ->
            ReorderableItem(reorderableLazyColumnState, row.key) { isDragging ->
                val interactionSource = remember { MutableInteractionSource() }

                when (row) {
                    is TabRow.Divider -> {
                        Column(
                            modifier = Modifier
                                .draggableHandle(
                                    enabled = false,
                                    interactionSource = interactionSource,
                                )
                        ) {
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            Text(
                                text = mokoString(row.text),

                                )
                        }
                    }
                    is TabRow.Placeholder -> {
                        Spacer(modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp))
                    }
                    is TabRow.Tab -> {
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                        val dividerIndex = combinedList.indexOfFirst { it is TabRow.Divider }
                        val currentIndex = index
                        val isBelowDivider = currentIndex > dividerIndex

                        val ghostAlpha by animateFloatAsState(if (isBelowDivider) 0.6f else 1f)

                        Box(modifier = Modifier.graphicsLayer { alpha = ghostAlpha }) {
                            TabItemCard(
                                modifier = Modifier.draggableHandle(enabled = true),
                                tab = row.item,
                                useServiceNavLogos = useServiceNavLogos,
                                isDragging = isDragging,
                                elevation = elevation
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabItemCard(
    tab: TabItem,
    useServiceNavLogos: Boolean,
    isDragging: Boolean,
    elevation: Dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = if (isDragging) 4.dp.toPx() else 0f
            },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                when (tab) {
                    is TabItem.Standard -> {
                        val logo = tab.associatedType?.tabIcon
                        if (useServiceNavLogos && logo != null) {
                            Icon(
                                painter = painterResource(logo),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = tab.androidIcon,
                                contentDescription = null
                            )
                        }
                        Text(
                            text = mokoString(tab.resource),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    is TabItem.CustomWebpage -> {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null
                        )
                        Column {
                            Text(
                                text = tab.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = tab.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TabItem.Settings -> {
                        // Settings shouldn't appear here, but handle it just in case
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                        Text(
                            text = mokoString(tab.resource),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

sealed class TabRow(val key: String) {
    data class Divider(val text: StringResource) : TabRow("divider_$text")
    data class Tab(val item: TabItem, val isActive: Boolean) : TabRow(item.key)
    object Placeholder : TabRow("placeholder_key")

    companion object {
        fun buildList(
            visibleTabs: List<TabItem>,
            hiddenTabs: List<TabItem>
        ) = buildList {
            addAll(visibleTabs.map { Tab(it, isActive = true) })
            add(Divider(MR.strings.navigation_items_drawer))

            if (hiddenTabs.isEmpty()) {
                add(Placeholder)
            } else {
                addAll(hiddenTabs.map { Tab(it, isActive = false) })
            }
        }
    }
}