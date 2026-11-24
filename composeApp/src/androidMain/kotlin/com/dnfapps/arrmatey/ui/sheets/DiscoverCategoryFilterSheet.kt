package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.discover.model.DiscoverFilterState
import com.dnfapps.arrmatey.discover.model.DiscoverGenres
import com.dnfapps.arrmatey.discover.model.DiscoverLanguages
import com.dnfapps.arrmatey.discover.model.DiscoverSortBy
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.Genre
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiscoverCategoryFilterSheet(
    filterState: DiscoverFilterState,
    rawItems: List<DiscoverResult> = emptyList(),
    genres: List<Genre> = emptyList(),
    onFilterStateChanged: (DiscoverFilterState) -> Unit,
    onResetFilterState: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val displayGenres = remember(genres, rawItems) {
        if (genres.isNotEmpty()) {
            genres.map { it.id.toInt() to it.name }
        } else {
            val genreIds = rawItems.flatMap { it.genreIds }.toSet().sorted()
            genreIds.map { it to DiscoverGenres.getGenreName(it) }
        }
    }

    val displayLanguages = remember {
        DiscoverLanguages.allLanguages
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = mokoString(MR.strings.filter_and_sort),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (filterState.isActive) {
                    IconButton(onClick = onResetFilterState) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = mokoString(MR.strings.reset),
                        )
                    }
                }
            }

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
                            val newOrder = if (filterState.sortOrder == SortOrder.Asc) {
                                SortOrder.Desc
                            } else {
                                SortOrder.Asc
                            }
                            onFilterStateChanged(filterState.copy(sortOrder = newOrder))
                        },
                        label = {
                            Text(
                                mokoString(filterState.sortOrder.resource),
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (filterState.sortOrder == SortOrder.Asc) {
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
                    DiscoverSortBy.entries.forEach { option ->
                        val isSelected = filterState.sortBy == option
                        FilterChip(
                            selected = isSelected,
                            onClick = { onFilterStateChanged(filterState.copy(sortBy = option)) },
                            label = { Text(mokoString(option.resource)) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                            } else {
                                null
                            },
                            shape = MaterialTheme.shapes.small,
                        )
                    }
                }
            }

            if (displayGenres.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = mokoString(MR.strings.genres),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        val isAllGenresSelected = filterState.selectedGenreId == null
                        FilterChip(
                            selected = isAllGenresSelected,
                            onClick = { onFilterStateChanged(filterState.copy(selectedGenreId = null)) },
                            label = { Text(mokoString(MR.strings.all_genres)) },
                            leadingIcon = if (isAllGenresSelected) {
                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                            } else {
                                null
                            },
                            shape = MaterialTheme.shapes.small,
                        )

                        displayGenres.forEach { (genreId, genreName) ->
                            val isSelected = filterState.selectedGenreId == genreId
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val newGenre = if (isSelected) null else genreId
                                    onFilterStateChanged(filterState.copy(selectedGenreId = newGenre))
                                },
                                label = { Text(genreName) },
                                leadingIcon = if (isSelected) {
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

            if (displayLanguages.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = mokoString(MR.strings.original_language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        val isAllLangsSelected = filterState.selectedLanguage == null
                        FilterChip(
                            selected = isAllLangsSelected,
                            onClick = { onFilterStateChanged(filterState.copy(selectedLanguage = null)) },
                            label = { Text(mokoString(MR.strings.all_languages)) },
                            leadingIcon = if (isAllLangsSelected) {
                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                            } else {
                                null
                            },
                            shape = MaterialTheme.shapes.small,
                        )

                        displayLanguages.forEach { (langCode, langName) ->
                            val isSelected = filterState.selectedLanguage.equals(langCode, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val newLang = if (isSelected) null else langCode
                                    onFilterStateChanged(filterState.copy(selectedLanguage = newLang))
                                },
                                label = { Text(langName) },
                                leadingIcon = if (isSelected) {
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

            ContainerCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                    ) {
                        Text(
                            text = mokoString(MR.strings.hide_in_library_content),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = mokoString(MR.strings.hide_in_library_content_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = filterState.hideInLibrary,
                        onCheckedChange = { checked ->
                            onFilterStateChanged(filterState.copy(hideInLibrary = checked))
                        },
                    )
                }
            }
        }
    }
}
