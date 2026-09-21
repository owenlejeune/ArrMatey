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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.CustomFilter
import com.dnfapps.arrmatey.arr.api.model.CustomFormat
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.QualityInfo
import com.dnfapps.arrmatey.arr.api.model.ReleaseProtocol
import com.dnfapps.arrmatey.arr.state.ReleaseLibrary
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.compose.utils.ReleaseSortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InteractiveSearchMenu(
    type: InstanceType,
    selectedFilter: ReleaseFilterBy,
    onFilterChanged: (ReleaseFilterBy) -> Unit,
    selectedSortOrder: SortOrder,
    onSortOrderChanged: (SortOrder) -> Unit,
    selectedSortBy: ReleaseSortBy,
    onSortByChanged: (ReleaseSortBy) -> Unit,
    libraryState: ReleaseLibrary.Success?,
    filterLanguage: Language?,
    onLanguageChange: (Language?) -> Unit,
    filterCustomFormat: CustomFormat?,
    onCustomFormatChange: (CustomFormat?) -> Unit,
    filterQualityInfo: QualityInfo?,
    onQualityChange: (QualityInfo?) -> Unit,
    filterIndexer: String?,
    onIndexerChange: (String?) -> Unit,
    filterProtocol: ReleaseProtocol?,
    onProtocolChange: (ReleaseProtocol?) -> Unit,
    customFilters: List<CustomFilter>,
    selectedCustomFilterId: Long?,
    onCustomFilterChange: (Long?) -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }

    val releaseFilters =
        remember(customFilters) {
            customFilters.filter { it.type == "release" || it.type == "releases" }
        }

    val activeFiltersCount =
        remember(
            selectedFilter,
            filterLanguage,
            filterCustomFormat,
            filterQualityInfo,
            filterIndexer,
            filterProtocol,
            selectedCustomFilterId,
        ) {
            var count = 0
            if (filterQualityInfo != null) count++
            if (filterLanguage != null) count++
            if (filterCustomFormat != null) count++
            if (filterProtocol != null) count++
            if (filterIndexer != null) count++
            if (type == InstanceType.Sonarr && selectedFilter != ReleaseFilterBy.Any) count++
            if (selectedCustomFilterId != null) count++
            count
        }

    Box {
        IconButton(onClick = { showSheet = true }) {
            BadgedBox(
                badge = {
                    if (activeFiltersCount > 0) {
                        Badge { Text(activeFiltersCount.toString()) }
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = mokoString(MR.strings.filter),
                    tint = if (activeFiltersCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
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
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = mokoString(MR.strings.filters),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (activeFiltersCount > 0) {
                            TextButton(
                                onClick = {
                                    onQualityChange(null)
                                    onLanguageChange(null)
                                    onCustomFormatChange(null)
                                    onProtocolChange(null)
                                    onIndexerChange(null)
                                    if (type == InstanceType.Sonarr) {
                                        onFilterChanged(ReleaseFilterBy.Any)
                                    }
                                    onCustomFilterChange(null)
                                },
                            ) {
                                Text(
                                    text = mokoString(MR.strings.clear_all),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }

                    // Sort By Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = mokoString(MR.strings.sort_by),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )

                            FilterChip(
                                selected = true,
                                onClick = {
                                    onSortOrderChanged(
                                        if (selectedSortOrder == SortOrder.Asc) SortOrder.Desc else SortOrder.Asc,
                                    )
                                },
                                label = {
                                    Text(
                                        if (selectedSortOrder == SortOrder.Asc) {
                                            mokoString(MR.strings.sort_ascending)
                                        } else {
                                            mokoString(MR.strings.sort_descending)
                                        },
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                            if (selectedSortOrder == SortOrder.Asc) {
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

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
                        ) {
                            ReleaseSortBy.entries.forEach { sort ->
                                val isSelected = selectedSortBy == sort
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSortByChanged(sort) },
                                    label = { Text(mokoString(sort.resource)) },
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

                    // Sonarr Release Filter
                    if (type == InstanceType.Sonarr) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = mokoString(MR.strings.filter_by),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp),
                            ) {
                                ReleaseFilterBy.entries.forEach { filter ->
                                    val isSelected = filter == selectedFilter && selectedCustomFilterId == null
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            onFilterChanged(filter)
                                            if (selectedCustomFilterId != null) {
                                                onCustomFilterChange(null)
                                            }
                                        },
                                        label = { Text(mokoString(filter.resource)) },
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

                    // Custom Filters
                    if (releaseFilters.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = mokoString(MR.strings.custom_filters),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp),
                            ) {
                                releaseFilters.forEach { filter ->
                                    val isSelected = filter.id == selectedCustomFilterId
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            onCustomFilterChange(if (isSelected) null else filter.id)
                                        },
                                        label = { Text(filter.label) },
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

                    // Qualities Filter
                    libraryState?.filterQualities?.let { qualities ->
                        if (qualities.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = mokoString(MR.strings.quality_profile),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    val isAnySelected = filterQualityInfo == null
                                    FilterChip(
                                        selected = isAnySelected,
                                        onClick = { onQualityChange(null) },
                                        label = { Text(mokoString(MR.strings.any)) },
                                        leadingIcon =
                                            if (isAnySelected) {
                                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                            } else {
                                                null
                                            },
                                        shape = MaterialTheme.shapes.small,
                                    )
                                    qualities.forEach { info ->
                                        val isSelected = filterQualityInfo == info
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onQualityChange(if (isSelected) null else info) },
                                            label = { Text(info.qualityLabel) },
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

                    // Languages Filter
                    libraryState?.filterLanguages?.let { languages ->
                        if (languages.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = mokoString(MR.strings.language),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    val isAnySelected = filterLanguage == null
                                    FilterChip(
                                        selected = isAnySelected,
                                        onClick = { onLanguageChange(null) },
                                        label = { Text(mokoString(MR.strings.any)) },
                                        leadingIcon =
                                            if (isAnySelected) {
                                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                            } else {
                                                null
                                            },
                                        shape = MaterialTheme.shapes.small,
                                    )
                                    languages.forEach { lang ->
                                        val isSelected = filterLanguage == lang
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onLanguageChange(if (isSelected) null else lang) },
                                            label = { Text(lang.name ?: "") },
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

                    // Custom Formats Filter
                    libraryState?.filterCustomFormats?.let { customFormats ->
                        if (customFormats.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = mokoString(MR.strings.custom_format),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    val isAnySelected = filterCustomFormat == null
                                    FilterChip(
                                        selected = isAnySelected,
                                        onClick = { onCustomFormatChange(null) },
                                        label = { Text(mokoString(MR.strings.any)) },
                                        leadingIcon =
                                            if (isAnySelected) {
                                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                            } else {
                                                null
                                            },
                                        shape = MaterialTheme.shapes.small,
                                    )
                                    customFormats.forEach { cf ->
                                        val isSelected = filterCustomFormat == cf
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onCustomFormatChange(if (isSelected) null else cf) },
                                            label = { Text(cf.name) },
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

                    // Protocol Filter
                    libraryState?.filterProtocols?.let { protocols ->
                        if (protocols.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = mokoString(MR.strings.protocol),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    val isAnySelected = filterProtocol == null
                                    FilterChip(
                                        selected = isAnySelected,
                                        onClick = { onProtocolChange(null) },
                                        label = { Text(mokoString(MR.strings.any)) },
                                        leadingIcon =
                                            if (isAnySelected) {
                                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                            } else {
                                                null
                                            },
                                        shape = MaterialTheme.shapes.small,
                                    )
                                    protocols.forEach { proto ->
                                        val isSelected = filterProtocol == proto
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onProtocolChange(if (isSelected) null else proto) },
                                            label = { Text(proto.name) },
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

                    // Indexer Filter
                    libraryState?.filterIndexers?.let { indexers ->
                        if (indexers.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = mokoString(MR.strings.indexer),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    val isAnySelected = filterIndexer == null
                                    FilterChip(
                                        selected = isAnySelected,
                                        onClick = { onIndexerChange(null) },
                                        label = { Text(mokoString(MR.strings.any)) },
                                        leadingIcon =
                                            if (isAnySelected) {
                                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                            } else {
                                                null
                                            },
                                        shape = MaterialTheme.shapes.small,
                                    )
                                    indexers.forEach { indexer ->
                                        val isSelected = filterIndexer == indexer
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onIndexerChange(if (isSelected) null else indexer) },
                                            label = { Text(indexer) },
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
    }
}
