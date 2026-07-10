package com.dnfapps.arrmatey.ui.menu

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    onCustomFilterChange: (Long?) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val groupInteractionSource = remember { MutableInteractionSource() }

    val indexes = if (type == InstanceType.Sonarr) {
        listOf(0, 1, 2)
    } else { listOf(0, null, 1) }
    val indexCount = indexes.count { it != null }

    Box {
        IconButton(onClick = {
            menuExpanded = true
        }) {
            Icon(Icons.Default.FilterList, null)
        }
        DropdownMenuPopup(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, indexCount),
                interactionSource = groupInteractionSource
            ) {
                libraryState?.let { state ->
                    QualitiesMenu(state.filterQualities, filterQualityInfo) {
                        onQualityChange(it)
                        menuExpanded = false
                    }
                    LanguageMenu(state.filterLanguages, filterLanguage) {
                        onLanguageChange(it)
                        menuExpanded = false
                    }
                    CustomFormatMenu(state.filterCustomFormats, filterCustomFormat) {
                        onCustomFormatChange(it)
                        menuExpanded = false
                    }
                    ProtocolMenu(state.filterProtocols, filterProtocol) {
                        onProtocolChange(it)
                        menuExpanded = false
                    }
                    IndexersMenu(state.filterIndexers, filterIndexer) {
                        onIndexerChange(it)
                        menuExpanded = false
                    }
                }
            }
            Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))

            if (type == InstanceType.Sonarr) {
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(1, indexCount),
                    interactionSource = groupInteractionSource
                ) {
                    ReleaseFilterBy.entries.forEachIndexed { index, filter ->
                        DropdownMenuItem(
                            text = { Text(mokoString(filter.resource)) },
                            selected = filter == selectedFilter && selectedCustomFilterId == null,
                            onClick = {
                                onFilterChanged(filter)
                                menuExpanded = false
                            },
                            shapes = MenuDefaults.itemShape(index, ReleaseFilterBy.entries.size),
                            selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
            }

            val releaseFilters = customFilters.filter { it.type == "release" || it.type == "releases" }
            if (releaseFilters.isNotEmpty()) {
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(indexCount, indexCount + 1),
                    interactionSource = groupInteractionSource
                ) {
                    releaseFilters.forEachIndexed { index, filter ->
                        DropdownMenuItem(
                            text = { Text(filter.label) },
                            selected = filter.id == selectedCustomFilterId,
                            onClick = {
                                onCustomFilterChange(if (selectedCustomFilterId == filter.id) null else filter.id)
                                menuExpanded = false
                            },
                            shapes = MenuDefaults.itemShape(index, releaseFilters.size),
                            selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
            }

            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(indexes[2]!!, indexCount + (if (releaseFilters.isNotEmpty()) 1 else 0)),
                interactionSource = groupInteractionSource
            ) {
                ReleaseSortBy.entries.forEachIndexed { index, sort ->
                    DropdownMenuItem(
                        text = { Text(mokoString(sort.resource)) },
                        selected = sort == selectedSortBy,
                        onClick = {
                            if (sort == selectedSortBy) {
                                onSortOrderChanged(when (selectedSortOrder) {
                                    SortOrder.Asc -> SortOrder.Desc
                                    SortOrder.Desc -> SortOrder.Asc
                                })
                            } else {
                                onSortByChanged(sort)
                            }
                            menuExpanded = false
                        },
                        shapes = MenuDefaults.itemShape(index, ReleaseSortBy.entries.size),
                        selectedLeadingIcon = { when (selectedSortOrder) {
                            SortOrder.Asc -> Icon(Icons.Default.ArrowDropUp, null)
                            SortOrder.Desc -> Icon(Icons.Default.ArrowDropDown, null)
                        } }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QualitiesMenu(
    qualities: Set<QualityInfo>,
    selected: QualityInfo?,
    onChange: (QualityInfo?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    Box {
        DropdownMenuItem(
            text = { Text(selected?.qualityLabel ?: mokoString(MR.strings.quality_profile)) },
            onClick = { expanded = true },
            trailingIcon = { Icon(Icons.Default.ChevronRight, null) },
            leadingIcon = { Icon(Icons.Default.HighQuality, null) }
        )
        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 350.dp, y = 0.dp)
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 1),
                interactionSource = interactionSource,
                containerColor = MenuDefaults.groupVibrantContainerColor
            ) {
                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.any)) },
                    selected = selected == null,
                    onClick = {
                        onChange(null)
                        expanded = false
                    },
                    shapes = MenuDefaults.itemShape(0, qualities.size+1),
                    colors = MenuDefaults.selectableItemVibrantColors(),
                    selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                )
                qualities.forEachIndexed { index, info ->
                    DropdownMenuItem(
                        text = { Text(info.qualityLabel) },
                        onClick = {
                            onChange(info)
                            expanded = false
                        },
                        selected = info == selected,
                        shapes = MenuDefaults.itemShape(index+1, qualities.size+1),
                        colors = MenuDefaults.selectableItemVibrantColors(),
                        selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LanguageMenu(
    languages: Set<Language>,
    selected: Language?,
    onChange: (Language?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    Box {
        DropdownMenuItem(
            text = { Text(selected?.name ?: mokoString(MR.strings.language)) },
            onClick = { expanded = true },
            trailingIcon = { Icon(Icons.Default.ChevronRight, null) },
            leadingIcon = { Icon(Icons.Default.Language, null) }
        )
        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 350.dp, y = 0.dp)
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 1),
                interactionSource = interactionSource,
                containerColor = MenuDefaults.groupVibrantContainerColor
            ) {
                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.any)) },
                    selected = selected == null,
                    onClick = {
                        onChange(null)
                        expanded = false
                    },
                    shapes = MenuDefaults.itemShape(0, languages.size+1),
                    colors = MenuDefaults.selectableItemVibrantColors(),
                    selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                )
                languages.forEachIndexed { index, language ->
                    DropdownMenuItem(
                        text = { Text(language.name ?: mokoString(MR.strings.unknown)) },
                        onClick = {
                            onChange(language)
                            expanded = false
                        },
                        selected = language == selected,
                        shapes = MenuDefaults.itemShape(index+1, languages.size+1),
                        colors = MenuDefaults.selectableItemVibrantColors(),
                        selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IndexersMenu(
    indexers: Set<String>,
    selected: String?,
    onChange: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    Box {
        DropdownMenuItem(
            text = { Text(selected ?: mokoString(MR.strings.indexer)) },
            onClick = { expanded = true },
            trailingIcon = { Icon(Icons.Default.ChevronRight, null) },
            leadingIcon = { Icon(Icons.Default.House, null) }
        )
        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 350.dp, y = 0.dp)
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 1),
                interactionSource = interactionSource,
                containerColor = MenuDefaults.groupVibrantContainerColor
            ) {
                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.any)) },
                    selected = selected == null,
                    onClick = {
                        onChange(null)
                        expanded = false
                    },
                    shapes = MenuDefaults.itemShape(0, indexers.size+1),
                    colors = MenuDefaults.selectableItemVibrantColors(),
                    selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                )
                indexers.forEachIndexed { index, indexer ->
                    DropdownMenuItem(
                        text = { Text(indexer) },
                        onClick = {
                            onChange(indexer)
                            expanded = false
                        },
                        selected = indexer == selected,
                        shapes = MenuDefaults.itemShape(index+1, indexers.size+1),
                        colors = MenuDefaults.selectableItemVibrantColors(),
                        selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProtocolMenu(
    protocols: Set<ReleaseProtocol>,
    selected: ReleaseProtocol?,
    onChange: (ReleaseProtocol?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    Box {
        DropdownMenuItem(
            text = { Text(selected?.name ?: mokoString(MR.strings.protocol)) },
            onClick = { expanded = true },
            trailingIcon = { Icon(Icons.Default.ChevronRight, null) },
            leadingIcon = { Icon(Icons.Default.Download, null) }
        )
        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 350.dp, y = 0.dp)
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 1),
                interactionSource = interactionSource,
                containerColor = MenuDefaults.groupVibrantContainerColor
            ) {
                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.any)) },
                    selected = selected == null,
                    onClick = {
                        onChange(null)
                        expanded = false
                    },
                    shapes = MenuDefaults.itemShape(0, protocols.size+1),
                    colors = MenuDefaults.selectableItemVibrantColors(),
                    selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                )
                protocols.forEachIndexed { index, protocol ->
                    DropdownMenuItem(
                        text = { Text(protocol.name) },
                        onClick = {
                            onChange(protocol)
                            expanded = false
                        },
                        selected = protocol == selected,
                        shapes = MenuDefaults.itemShape(index+1, protocols.size+1),
                        colors = MenuDefaults.selectableItemVibrantColors(),
                        selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CustomFormatMenu(
    customFormats: Set<CustomFormat>,
    selected: CustomFormat?,
    onChange: (CustomFormat?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    Box {
        DropdownMenuItem(
            text = { Text(selected?.name ?: mokoString(MR.strings.custom_format)) },
            onClick = { expanded = true },
            trailingIcon = { Icon(Icons.Default.ChevronRight, null) },
            leadingIcon = { Icon(Icons.Default.Tag, null) }
        )
        DropdownMenuPopup(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 350.dp, y = 0.dp)
        ) {
            DropdownMenuGroup(
                shapes = MenuDefaults.groupShape(0, 1),
                interactionSource = interactionSource,
                containerColor = MenuDefaults.groupVibrantContainerColor
            ) {
                DropdownMenuItem(
                    text = { Text(mokoString(MR.strings.any)) },
                    selected = selected == null,
                    onClick = {
                        onChange(null)
                        expanded = false
                    },
                    shapes = MenuDefaults.itemShape(0, customFormats.size+1),
                    colors = MenuDefaults.selectableItemVibrantColors(),
                    selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                )
                customFormats.forEachIndexed { index, format ->
                    DropdownMenuItem(
                        text = { Text(format.name) },
                        onClick = {
                            onChange(format)
                            expanded = false
                        },
                        selected = format == selected,
                        shapes = MenuDefaults.itemShape(index+1, customFormats.size+1),
                        colors = MenuDefaults.selectableItemVibrantColors(),
                        selectedLeadingIcon = { Icon(Icons.Default.Check, null) }
                    )
                }
            }
        }
    }
}
