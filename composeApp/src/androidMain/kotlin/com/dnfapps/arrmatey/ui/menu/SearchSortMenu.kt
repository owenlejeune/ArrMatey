package com.dnfapps.arrmatey.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchSortMenu(
    sortBy: SortBy,
    onSortChanged: (SortBy) -> Unit,
    sortOrder: SortOrder,
    onOrderChanged: (SortOrder) -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }
    val options = remember { SortBy.lookupEntries() }

    Box {
        IconButton(
            onClick = { showSheet = true },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Sort,
                contentDescription = mokoString(MR.strings.sort),
            )
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // Header with sort direction toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = mokoString(MR.strings.sort),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )

                        FilterChip(
                            selected = true,
                            onClick = {
                                onOrderChanged(
                                    if (sortOrder == SortOrder.Asc) SortOrder.Desc else SortOrder.Asc,
                                )
                            },
                            label = {
                                Text(
                                    if (sortOrder == SortOrder.Asc) {
                                        mokoString(MR.strings.sort_ascending)
                                    } else {
                                        mokoString(MR.strings.sort_descending)
                                    },
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        if (sortOrder == SortOrder.Asc) {
                                            Icons.Default.ArrowUpward
                                        } else {
                                            Icons.Default.ArrowDownward
                                        },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                            shape = MaterialTheme.shapes.small,
                        )
                    }

                    // Sort Criteria
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = mokoString(MR.strings.sort_by),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            options.forEach { option ->
                                val isSelected = option == sortBy
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSortChanged(option) },
                                    label = { Text(mokoString(option.resource)) },
                                    leadingIcon =
                                        if (isSelected) {
                                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                        } else {
                                            null
                                        },
                                    shape = MaterialTheme.shapes.small,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
