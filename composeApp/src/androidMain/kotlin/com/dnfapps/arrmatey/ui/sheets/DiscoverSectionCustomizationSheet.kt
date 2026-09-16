package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.datastore.DiscoverSectionPreferences
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverSectionCustomizationSheet(
    preferences: DiscoverSectionPreferences,
    onUpdatePreferences: (DiscoverSectionPreferences) -> Unit,
    onResetPreferences: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = navigationBarBottomInset() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = mokoString(MR.strings.discover_sections),
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(onClick = onResetPreferences) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (preferences.visibleCategories.isNotEmpty()) {
                    item(key = "header_visible") {
                        Text(
                            text = mokoString(MR.strings.navigation_items_visible),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.animateItem(),
                        )
                    }

                    itemsIndexed(
                        items = preferences.visibleCategories,
                        key = { _, category -> category.name },
                    ) { index, category ->
                        ContainerCard(
                            modifier = Modifier.animateItem(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = mokoString(category.title),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            if (index > 0) {
                                                val newList = preferences.visibleCategories.toMutableList()
                                                val item = newList.removeAt(index)
                                                newList.add(index - 1, item)
                                                onUpdatePreferences(
                                                    preferences.copy(visibleCategories = newList),
                                                )
                                            }
                                        },
                                        enabled = index > 0,
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                                    }

                                    IconButton(
                                        onClick = {
                                            if (index < preferences.visibleCategories.lastIndex) {
                                                val newList = preferences.visibleCategories.toMutableList()
                                                val item = newList.removeAt(index)
                                                newList.add(index + 1, item)
                                                onUpdatePreferences(
                                                    preferences.copy(visibleCategories = newList),
                                                )
                                            }
                                        },
                                        enabled = index < preferences.visibleCategories.lastIndex,
                                    ) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                                    }

                                    IconButton(
                                        onClick = {
                                            if (preferences.visibleCategories.size > 1) {
                                                val newVisible = preferences.visibleCategories.toMutableList().apply { removeAt(index) }
                                                val newHidden = preferences.hiddenCategories + category
                                                onUpdatePreferences(
                                                    preferences.copy(
                                                        visibleCategories = newVisible,
                                                        hiddenCategories = newHidden,
                                                    ),
                                                )
                                            }
                                        },
                                        enabled = preferences.visibleCategories.size > 1,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Hide Section",
                                            tint =
                                                if (preferences.visibleCategories.size > 1) {
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

                if (preferences.hiddenCategories.isNotEmpty()) {
                    item(key = "header_hidden") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = mokoString(MR.strings.navigation_items_hidden),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.animateItem(),
                        )
                    }

                    itemsIndexed(
                        items = preferences.hiddenCategories,
                        key = { _, category -> category.name },
                    ) { index, category ->
                        ContainerCard(
                            modifier = Modifier.animateItem(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = mokoString(category.title),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                )

                                IconButton(
                                    onClick = {
                                        val newHidden = preferences.hiddenCategories.toMutableList().apply { removeAt(index) }
                                        val newVisible = preferences.visibleCategories + category
                                        onUpdatePreferences(
                                            preferences.copy(
                                                visibleCategories = newVisible,
                                                hiddenCategories = newHidden,
                                            ),
                                        )
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Show Section",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
