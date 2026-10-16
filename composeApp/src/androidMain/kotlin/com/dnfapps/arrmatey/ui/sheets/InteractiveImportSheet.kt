package com.dnfapps.arrmatey.ui.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ManualImportFile
import com.dnfapps.arrmatey.arr.api.model.ManualImportMode
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.import_selected
import com.dnfapps.arrmatey.shared.interactive_import
import com.dnfapps.arrmatey.shared.interactive_import_scanned_description
import com.dnfapps.arrmatey.shared.none
import com.dnfapps.arrmatey.shared.select_all
import com.dnfapps.arrmatey.shared.select_artist
import com.dnfapps.arrmatey.shared.select_book
import com.dnfapps.arrmatey.shared.select_episode
import com.dnfapps.arrmatey.shared.select_media
import com.dnfapps.arrmatey.shared.select_movie
import com.dnfapps.arrmatey.shared.select_series
import com.dnfapps.arrmatey.shared.selected_count
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

data class InteractiveImportFileItem(
    val id: String,
    val file: ManualImportFile,
    val isSelected: Boolean,
    val mappedMediaId: Long? = null,
    val mappedMediaTitle: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveImportSheet(
    instance: Instance?,
    folderPath: String,
    importMode: ManualImportMode,
    initialFiles: List<ManualImportFile>,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ActivityQueueViewModel,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        InteractiveImportContent(
            instance = instance,
            folderPath = folderPath,
            importMode = importMode,
            initialFiles = initialFiles,
            onSuccess = onSuccess,
            viewModel = viewModel,
        )
    }
}

@Composable
fun InteractiveImportContent(
    instance: Instance?,
    folderPath: String,
    importMode: ManualImportMode,
    initialFiles: List<ManualImportFile>,
    onSuccess: () -> Unit,
    viewModel: ActivityQueueViewModel,
    modifier: Modifier = Modifier,
) {
    var fileItems by remember {
        mutableStateOf(
            initialFiles.map { f ->
                val initialId = f.effectiveSeriesId ?: f.effectiveMovieId ?: f.artistId ?: f.bookId
                val initialTitle = f.series?.title ?: f.movie?.title
                InteractiveImportFileItem(
                    id = f.stableId,
                    file = f,
                    isSelected = false,
                    mappedMediaId = initialId,
                    mappedMediaTitle = initialTitle,
                )
            },
        )
    }

    var itemForMediaSelection by remember { mutableStateOf<InteractiveImportFileItem?>(null) }
    var itemForEpisodeSelection by remember { mutableStateOf<InteractiveImportFileItem?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(instance?.id) {
        val instanceId = instance?.id ?: return@LaunchedEffect
        viewModel.getMediaForInstance(
            instanceId = instanceId,
            onSuccess = { mediaList ->
                fileItems = fileItems.map { item ->
                    if (item.mappedMediaId != null && item.mappedMediaTitle == null) {
                        val found = mediaList.firstOrNull { it.id == item.mappedMediaId }
                        if (found != null) {
                            item.copy(mappedMediaTitle = found.title)
                        } else {
                            item
                        }
                    } else {
                        item
                    }
                }
            },
            onError = {},
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = mokoString(MR.strings.interactive_import),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    onClick = {
                        fileItems = fileItems.map { it.copy(isSelected = true) }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = mokoString(MR.strings.select_all),
                    )
                }

                IconButton(
                    onClick = {
                        fileItems = fileItems.map { it.copy(isSelected = false) }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Deselect,
                        contentDescription = mokoString(MR.strings.none),
                    )
                }
            }
        }

        ContainerCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folderPath,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = mokoString(MR.strings.interactive_import_scanned_description, fileItems.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        errorMessage?.let { errorMsg ->
            ContainerCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = errorMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { errorMessage = null },
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(fileItems, key = { _, item -> item.id }) { index, item ->
                InteractiveImportFileRow(
                    item = item,
                    instanceType = instance?.type,
                    onToggle = {
                        fileItems = fileItems.toMutableList().also { list ->
                            list[index] = item.copy(isSelected = !item.isSelected)
                        }
                    },
                    onSelectMediaClick = {
                        itemForMediaSelection = item
                    },
                    onSelectEpisodeClick = {
                        itemForEpisodeSelection = item
                    },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val selectedCount = fileItems.count { it.isSelected }
            Text(
                text = mokoPlural(MR.plurals.selected_count, selectedCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = {
                    val instanceId = instance?.id ?: return@Button
                    val selectedFiles = fileItems.filter { it.isSelected }.map { it.file }
                    if (selectedFiles.isEmpty()) return@Button

                    isImporting = true
                    errorMessage = null
                    viewModel.executeManualImport(
                        instanceId = instanceId,
                        files = selectedFiles,
                        importMode = importMode.value,
                        onSuccess = {
                            isImporting = false
                            onSuccess()
                        },
                        onError = { err ->
                            isImporting = false
                            errorMessage = err
                        },
                    )
                },
                enabled = selectedCount > 0 && !isImporting,
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = mokoString(MR.strings.import_selected),
                    )
                }
            }
        }
    }

    itemForMediaSelection?.let { mappingItem ->
        SelectMediaDialog(
            instance = instance,
            currentSelectionId = mappingItem.mappedMediaId,
            onSelect = { media ->
                val mediaId = media.id ?: 0L
                fileItems = fileItems.map { it ->
                    if (it.id == mappingItem.id) {
                        val updatedFile = when (instance?.type) {
                            InstanceType.Sonarr -> it.file.withSeriesId(mediaId)
                            InstanceType.Radarr -> it.file.withMovieId(mediaId)
                            InstanceType.Lidarr -> it.file.withArtistId(mediaId)
                            InstanceType.Bookshelf, InstanceType.Chaptarr -> it.file.withBookId(mediaId)
                            else -> it.file
                        }
                        it.copy(
                            file = updatedFile,
                            mappedMediaId = mediaId,
                            mappedMediaTitle = media.title,
                        )
                    } else {
                        it
                    }
                }
                itemForMediaSelection = null
            },
            onDismiss = {
                itemForMediaSelection = null
            },
            viewModel = viewModel,
        )
    }

    itemForEpisodeSelection?.let { mappingItem ->
        val seriesId = mappingItem.mappedMediaId ?: mappingItem.file.effectiveSeriesId
        val instanceId = instance?.id
        if (seriesId != null && instanceId != null) {
            SelectEpisodeDialog(
                instanceId = instanceId,
                seriesId = seriesId,
                seriesTitle = mappingItem.mappedMediaTitle ?: mappingItem.file.series?.title,
                initialSeasonNumber = mappingItem.file.effectiveSeasonNumber,
                initialEpisodeIds = mappingItem.file.effectiveEpisodeIds,
                onSelect = { seasonNumber, episodes ->
                    fileItems = fileItems.map { it ->
                        if (it.id == mappingItem.id) {
                            val updatedFile = it.file.withEpisodes(episodes)
                            it.copy(file = updatedFile)
                        } else {
                            it
                        }
                    }
                    itemForEpisodeSelection = null
                },
                onDismiss = {
                    itemForEpisodeSelection = null
                },
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun InteractiveImportFileRow(
    item: InteractiveImportFileItem,
    instanceType: InstanceType?,
    onToggle: () -> Unit,
    onSelectMediaClick: () -> Unit,
    onSelectEpisodeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    ContainerCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
        ),
        border = if (item.isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        } else {
            null
        },
        contentPadding = PaddingValues(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(
                checked = item.isSelected,
                onCheckedChange = { onToggle() },
                modifier = Modifier.padding(end = 8.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.file.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.file.qualityLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = BULLET,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = item.file.sizeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (item.file.reasons.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        item.file.reasons.forEach { reason ->
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE65100),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))

                val mediaIcon: ImageVector = when (instanceType) {
                    InstanceType.Sonarr -> Icons.Default.Tv
                    InstanceType.Radarr -> Icons.Default.Movie
                    InstanceType.Lidarr -> Icons.Default.MusicNote
                    InstanceType.Bookshelf, InstanceType.Chaptarr -> Icons.AutoMirrored.Filled.MenuBook
                    else -> Icons.Default.PlayArrow
                }

                val defaultMediaLabel = when (instanceType) {
                    InstanceType.Sonarr -> mokoString(MR.strings.select_series)
                    InstanceType.Radarr -> mokoString(MR.strings.select_movie)
                    InstanceType.Lidarr -> mokoString(MR.strings.select_artist)
                    InstanceType.Bookshelf, InstanceType.Chaptarr -> mokoString(MR.strings.select_book)
                    else -> mokoString(MR.strings.select_media)
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    AssistChip(
                        onClick = onSelectMediaClick,
                        label = {
                            Text(
                                text = item.mappedMediaTitle ?: defaultMediaLabel,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = mediaIcon,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize),
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (item.mappedMediaTitle != null) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLowest
                            },
                            labelColor = if (item.mappedMediaTitle != null) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            leadingIconContentColor = if (item.mappedMediaTitle != null) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = Color.Transparent,
                        ),
                    )

                    val seriesId = item.mappedMediaId ?: item.file.effectiveSeriesId
                    if (instanceType == InstanceType.Sonarr && seriesId != null) {
                        val hasEpisodes = item.file.episodes.isNotEmpty() || item.file.effectiveEpisodeIds.isNotEmpty()
                        val episodeLabel = formatEpisodeLabel(item.file)

                        AssistChip(
                            onClick = { onSelectEpisodeClick?.invoke() },
                            label = {
                                Text(
                                    text = episodeLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(AssistChipDefaults.IconSize),
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (hasEpisodes) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                },
                                labelColor = if (hasEpisodes) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                leadingIconContentColor = if (hasEpisodes) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = Color.Transparent,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun formatEpisodeLabel(file: ManualImportFile): String {
    val season = file.effectiveSeasonNumber
    val episodes = file.episodes
    if (episodes.isNotEmpty()) {
        val epNumbers = episodes.map { it.episodeNumber }.sorted().joinToString("-")
        val s = season?.let { "S$it:" } ?: ""
        val title = episodes.firstOrNull()?.title?.takeIf { it.isNotBlank() }
        return if (title != null) "${s}E$epNumbers - $title" else "${s}E$epNumbers"
    } else if (file.effectiveEpisodeIds.isNotEmpty()) {
        val s = season?.let { "S$it:" } ?: ""
        return "${s}${file.effectiveEpisodeIds.size} Ep(s)"
    }
    return mokoString(MR.strings.select_episode)
}
