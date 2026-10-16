package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.cancel
import com.dnfapps.arrmatey.shared.done
import com.dnfapps.arrmatey.shared.episode
import com.dnfapps.arrmatey.shared.no_episodes_found_for_series
import com.dnfapps.arrmatey.shared.season_label
import com.dnfapps.arrmatey.shared.select_episode
import com.dnfapps.arrmatey.shared.specials
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun SelectEpisodeDialog(
    instanceId: Long,
    seriesId: Long,
    seriesTitle: String?,
    initialSeasonNumber: Int?,
    initialEpisodeIds: List<Long>,
    onSelect: (seasonNumber: Int?, episodes: List<Episode>) -> Unit,
    onDismiss: () -> Unit,
    viewModel: ActivityQueueViewModel,
) {
    var allEpisodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedSeason by remember { mutableIntStateOf(initialSeasonNumber ?: 1) }
    var selectedEpisodeIds by remember { mutableStateOf(initialEpisodeIds.toSet()) }

    LaunchedEffect(instanceId, seriesId) {
        viewModel.getEpisodesForSeries(
            instanceId = instanceId,
            seriesId = seriesId,
            seasonNumber = null,
            onSuccess = { list ->
                allEpisodes = list
                isLoading = false
                val seasons = list.map { it.seasonNumber }.distinct().sorted()
                if (initialSeasonNumber != null && seasons.contains(initialSeasonNumber)) {
                    selectedSeason = initialSeasonNumber
                } else if (seasons.isNotEmpty()) {
                    selectedSeason = seasons.firstOrNull { it > 0 } ?: seasons.first()
                }
            },
            onError = { err ->
                errorMessage = err
                isLoading = false
            },
        )
    }

    val availableSeasons = remember(allEpisodes) {
        allEpisodes.map { it.seasonNumber }.distinct().sorted()
    }

    val seasonEpisodes = remember(allEpisodes, selectedSeason) {
        allEpisodes.filter { it.seasonNumber == selectedSeason }
            .sortedBy { it.episodeNumber }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(min = 320.dp, max = 580.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = mokoString(MR.strings.select_episode),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    if (!seriesTitle.isNullOrBlank()) {
                        Text(
                            text = seriesTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else if (allEpisodes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = mokoString(MR.strings.no_episodes_found_for_series),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    if (availableSeasons.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(availableSeasons) { seasonNum ->
                                val label = if (seasonNum == 0) {
                                    mokoString(MR.strings.specials)
                                } else {
                                    mokoString(MR.strings.season_label, seasonNum)
                                }
                                FilterChip(
                                    selected = seasonNum == selectedSeason,
                                    onClick = { selectedSeason = seasonNum },
                                    label = { Text(label) },
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(seasonEpisodes, key = { it.id }) { ep ->
                            val isSelected = selectedEpisodeIds.contains(ep.id)
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedEpisodeIds = if (isSelected) {
                                            selectedEpisodeIds - ep.id
                                        } else {
                                            setOf(ep.id)
                                        }
                                    },
                                shape = MaterialTheme.shapes.small,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Text(
                                            text = "E${ep.episodeNumber}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Column {
                                            Text(
                                                text = ep.title ?: "${mokoString(MR.strings.episode)} ${ep.episodeNumber}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            ep.airDate?.let { date ->
                                                Text(
                                                    text = date.toString(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(mokoString(MR.strings.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val chosen = allEpisodes.filter { selectedEpisodeIds.contains(it.id) }
                            onSelect(selectedSeason, chosen)
                            onDismiss()
                        },
                        enabled = selectedEpisodeIds.isNotEmpty(),
                    ) {
                        Text(mokoString(MR.strings.done))
                    }
                }
            }
        }
    }
}
