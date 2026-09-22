package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaInstanceFilterRow(
    selectedFilter: InstanceType?,
    onFilterSelected: (InstanceType?) -> Unit,
    availableFilters: List<InstanceType>,
    itemCounts: Map<InstanceType, Int>,
    totalCount: Int,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
) {
    if (availableFilters.isEmpty()) return

    LazyRow(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // "All" chip
        item(key = "filter_all") {
            val isAllSelected = selectedFilter == null
            ElevatedFilterChip(
                selected = isAllSelected,
                onClick = { onFilterSelected(null) },
                label = {
                    FilterChipLabel(
                        title = mokoString(MR.strings.all),
                        count = totalCount,
                        isSelected = isAllSelected,
                        countContainerColor =
                            if (isAllSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                        countContentColor =
                            if (isAllSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                },
                leadingIcon = {
                    if (isAllSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                },
                shape = CircleShape,
                colors =
                    FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        }

        // Per-InstanceType chips
        items(availableFilters, key = { it.name }) { type ->
            val isSelected = selectedFilter == type
            val count = itemCounts[type] ?: 0
            val typeAccentColor = type.associatedColor

            val containerColor by animateColorAsState(
                targetValue =
                    if (isSelected) {
                        typeAccentColor.copy(alpha = 0.24f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                label = "chip_bg_${type.name}",
            )

            ElevatedFilterChip(
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        onFilterSelected(null)
                    } else {
                        onFilterSelected(type)
                    }
                },
                label = {
                    FilterChipLabel(
                        title = type.name,
                        count = count,
                        isSelected = isSelected,
                        countContainerColor =
                            if (isSelected) {
                                typeAccentColor.copy(alpha = 0.38f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                        countContentColor =
                            if (isSelected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                },
                leadingIcon = {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    } else {
                        Icon(
                            painter = painterResource(type.tabIcon),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = typeAccentColor,
                        )
                    }
                },
                shape = CircleShape,
                colors =
                    FilterChipDefaults.elevatedFilterChipColors(
                        containerColor = containerColor,
                        selectedContainerColor = containerColor,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                    ),
            )
        }
    }
}

@Composable
private fun FilterChipLabel(
    title: String,
    count: Int,
    isSelected: Boolean,
    countContainerColor: Color,
    countContentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
        )

        if (count > 0) {
            Box(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(countContainerColor)
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = countContentColor,
                )
            }
        }
    }
}
