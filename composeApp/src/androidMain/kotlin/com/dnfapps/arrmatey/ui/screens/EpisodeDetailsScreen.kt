package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.state.HistoryState
import com.dnfapps.arrmatey.arr.viewmodel.EpisodeDetailsViewModel
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.navigation.arrNavigator
import com.dnfapps.arrmatey.navigation.toSeriesRelease
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.EpisodeDetailsHeader
import com.dnfapps.arrmatey.ui.components.FileCard
import com.dnfapps.arrmatey.ui.components.HistoryItemView
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.ReleaseDownloadButtons
import com.dnfapps.arrmatey.ui.components.bazarr.BazarrSubtitlesSection
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun EpisodeDetailsScreen(
    series: ArrSeries,
    episode: Episode,
    viewModel: EpisodeDetailsViewModel = koinInjectParams(series.id, episode)
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val navigation = arrNavigator

    val currentEpisode by viewModel.episode.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val monitorStatus by viewModel.monitorStatus.collectAsStateWithLifecycle()
    val deleteStatus by viewModel.deleteStatus.collectAsStateWithLifecycle()

    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(monitorStatus) {
        when (val status = monitorStatus) {
            is OperationStatus.Success -> {
                Toast.makeText(context, status.message ?: "Updated", Toast.LENGTH_SHORT).show()
                viewModel.resetMonitorStatus()
            }
            is OperationStatus.Error -> {
                status.message?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                viewModel.resetMonitorStatus()
            }
            else -> {}
        }
    }

    LaunchedEffect(deleteStatus) {
        when (val status = deleteStatus) {
            is OperationStatus.Success -> {
                Toast.makeText(context, status.message ?: "Deleted", Toast.LENGTH_SHORT).show()
            }
            is OperationStatus.Error -> {
                status.message?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            OverlayTopAppBar(
                scrollState = scrollState,
                navigationIcon = {
                    IconButton(
                        onClick = { navigation.popBackStack() },
                        colors = IconButtonDefaults.headerBarColors()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleMonitor() },
                        colors = IconButtonDefaults.headerBarColors()
                    ) {
                        Icon(
                            imageVector = if (currentEpisode.monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = { confirmDelete = true },
                        colors = IconButtonDefaults.headerBarColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        enabled = episode.episodeFile != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues.copy(top = 0.dp, bottom = 0.dp))
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EpisodeDetailsHeader(currentEpisode, series)

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp).padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = episode.displayTitle,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    currentEpisode.overview?.let { overview ->
                        ItemDescriptionCard(overview)
                    }

                    ReleaseDownloadButtons(
                        onInteractiveClicked = {
                            navigation.toSeriesRelease(episodeId = currentEpisode.id)
                        },
                        onAutomaticClicked = {
                            viewModel.executeAutomaticSearch()
                        },
                        automaticSearchEnabled = currentEpisode.monitored
                    )

                    Text(
                        text = mokoString(MR.strings.files),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                    currentEpisode.episodeFile?.let { file ->
                        FileCard(file)
                    } ?: run {
                        Text(
                            text = mokoString(MR.strings.no_files),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    series.id?.let { seriesId ->
                        BazarrSubtitlesSection(
                            target = BazarrMediaTarget.Episode(seriesId, currentEpisode.id)
                        )
                    }

                    when (val historyResult = history) {
                        is HistoryState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is HistoryState.Success -> {
                            Text(
                                mokoString(MR.strings.history),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (historyResult.items.isEmpty()) {
                                Text(
                                    text = mokoString(MR.strings.no_history),
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                historyResult.items.forEach { historyItem ->
                                    HistoryItemView(historyItem)
                                }
                            }
                        }
                        is HistoryState.Error -> {}
                        else -> {}
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text(mokoString(MR.strings.are_you_sure)) },
                text = { Text(mokoString(MR.strings.episode_delete_message)) },
                dismissButton = {
                    TextButton(
                        onClick = { confirmDelete = false }
                    ) { Text(mokoString(MR.strings.cancel)) }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            confirmDelete = false
                            viewModel.deleteEpisode()
                        }
                    ) { Text(mokoString(MR.strings.yes)) }
                }
            )
        }
    }
}