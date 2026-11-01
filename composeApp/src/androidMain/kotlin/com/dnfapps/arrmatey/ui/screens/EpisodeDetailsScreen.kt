package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.state.HistoryState
import com.dnfapps.arrmatey.arr.viewmodel.EpisodeDetailsViewModel
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.ui.components.DetailHeaderBanner
import com.dnfapps.arrmatey.ui.components.FileCard
import com.dnfapps.arrmatey.ui.components.HistoryItemView
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.MediaActivitySection
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.ReleaseDownloadButtons
import com.dnfapps.arrmatey.ui.components.bazarr.BazarrSubtitlesSection
import com.dnfapps.arrmatey.ui.components.tracearr.TracearrAnalyticsSection
import com.dnfapps.arrmatey.ui.components.tracearr.TracearrHistorySection
import com.dnfapps.arrmatey.ui.components.tracearr.TracearrSummaryChipRow
import com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs.ConfirmDeleteEpisodeDialog
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.helpers.LocalIsInTwoPane
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrStreamDetailsSheet
import com.dnfapps.arrmatey.ui.sheets.ConfirmDeleteItemSheet
import com.dnfapps.arrmatey.ui.sheets.QueueItemInfoSheet
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.handleWatchClick
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private enum class EpisodeDetailsTab {
    Overview,
    Analytics,
    Streams,
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EpisodeDetailsScreen(
    series: ArrSeries,
    episode: Episode,
    isExpanded: Boolean = false,
    wideRailIsVisible: Boolean = false,
    onBack: () -> Unit = {},
    onNavigateToSeriesRelease: (Long) -> Unit = {},
    viewModel: EpisodeDetailsViewModel =
        koinViewModel(key = "${series.id}_${episode.id}", parameters = { parametersOf(series.id, episode, series) }),
) {
    val context = LocalContext.current
    val moko: MokoStrings = koinInject()
    val scrollState = rememberScrollState()

    val currentEpisode by viewModel.episode.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val monitorStatus by viewModel.monitorStatus.collectAsStateWithLifecycle()
    val deleteStatus by viewModel.deleteStatus.collectAsStateWithLifecycle()
    val queueItems by viewModel.queueItems.collectAsStateWithLifecycle()
    val removeQueueItemStatus by viewModel.removeQueueItemStatus.collectAsStateWithLifecycle()
    val tracearrState by viewModel.tracearrState.collectAsStateWithLifecycle()
    val mediaServerList by viewModel.mediaServerDetails.collectAsStateWithLifecycle()
    val firstMediaServer = mediaServerList.firstOrNull()

    var confirmDelete by remember { mutableStateOf(false) }
    var selectedQueueItem by remember { mutableStateOf<QueueItem?>(null) }
    var showConfirmRemoveQueueItem by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(EpisodeDetailsTab.Overview) }
    var selectedTracearrStreamSession by remember { mutableStateOf<TracearrStreamSession?>(null) }

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
                        onClick = onBack,
                        colors = IconButtonDefaults.headerBarColors(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(MR.strings.back),
                        )
                    }
                },
                actions = {
                    if (firstMediaServer != null) {
                        val provider = when (firstMediaServer.serverType) {
                            MediaServerType.Plex -> MediaProvider.Plex
                            MediaServerType.Jellyfin -> MediaProvider.Jellyfin
                            MediaServerType.Emby -> MediaProvider.Emby
                        }
                        IconButton(
                            onClick = {
                                handleWatchClick(
                                    firstMediaServer.deepLinkUrl,
                                    provider,
                                    context,
                                    moko,
                                    firstMediaServer.webFallbackUrl,
                                )
                            },
                            colors = IconButtonDefaults.headerBarColors(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Watch",
                                tint = firstMediaServer.serverType.brandColor,
                            )
                        }
                        val isWatched = firstMediaServer.played
                        IconButton(
                            onClick = {
                                viewModel.markMediaServerWatched(
                                    firstMediaServer.serverType,
                                    firstMediaServer.itemId,
                                    !isWatched,
                                )
                            },
                            colors = IconButtonDefaults.headerBarColors(),
                        ) {
                            Icon(
                                imageVector = if (isWatched) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                contentDescription = if (isWatched) "Mark as Unwatched" else "Mark as Watched",
                                tint = if (isWatched) ArrGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.toggleMonitor() },
                        colors = IconButtonDefaults.headerBarColors(),
                    ) {
                        Icon(
                            imageVector = if (currentEpisode.monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                        )
                    }
                    IconButton(
                        onClick = { confirmDelete = true },
                        colors =
                        IconButtonDefaults.headerBarColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                        enabled = currentEpisode.episodeFile != null,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
            Modifier
                .padding(paddingValues.copy(top = 0.dp, bottom = 0.dp))
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val isInTwoPane = LocalIsInTwoPane.current
                Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                    DetailHeaderBanner(
                        bannerUrl = currentEpisode.getBanner()?.remoteUrl,
                        gradientHeight = 50.dp,
                        startGradient = isExpanded && (wideRailIsVisible || isInTwoPane),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column {
                        Text(
                            text = currentEpisode.displayTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        series.title?.let { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = 24.dp),
                            )
                        }
                        val statusRow =
                            listOfNotNull(
                                currentEpisode.seasonEpLabel,
                                currentEpisode.runtimeString,
                                currentEpisode.formatAirDateUtc(),
                            ).joinToString(BULLET)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 24.dp),
                        ) {
                            Text(
                                text = statusRow,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            if (firstMediaServer != null) {
                                val isWatched = firstMediaServer.played
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable {
                                            viewModel.markMediaServerWatched(
                                                firstMediaServer.serverType,
                                                firstMediaServer.itemId,
                                                !isWatched,
                                            )
                                        }
                                        .padding(vertical = 2.dp, horizontal = 4.dp),
                                ) {
                                    Icon(
                                        imageVector = if (isWatched) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                                        contentDescription = if (isWatched) "Mark as Unwatched" else "Mark as Watched",
                                        tint = if (isWatched) ArrGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = if (isWatched) "Watched" else "Unwatched",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isWatched) ArrGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                        if (tracearrState.isTracearrConfigured) {
                            TracearrSummaryChipRow(
                                uiState = tracearrState,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }

                        if (tracearrState.isTracearrConfigured) {
                            val availableTabs =
                                listOf(
                                    EpisodeDetailsTab.Overview to mokoString(MR.strings.overview),
                                    EpisodeDetailsTab.Analytics to mokoString(MR.strings.statistics),
                                    EpisodeDetailsTab.Streams to mokoString(MR.strings.streams_label),
                                )
                            PrimaryScrollableTabRow(
                                selectedTabIndex =
                                availableTabs
                                    .indexOfFirst { it.first == selectedTab }
                                    .coerceAtLeast(0),
                                modifier = Modifier.fillMaxWidth(),
                                edgePadding = 0.dp,
                            ) {
                                availableTabs.forEach { (tab, label) ->
                                    Tab(
                                        selected = selectedTab == tab,
                                        onClick = { selectedTab = tab },
                                        text = { Text(label) },
                                    )
                                }
                            }
                        }
                    }

                    when (if (tracearrState.isTracearrConfigured) selectedTab else EpisodeDetailsTab.Overview) {
                        EpisodeDetailsTab.Overview -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                currentEpisode.overview?.let { overview ->
                                    ItemDescriptionCard(
                                        overview = overview,
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                    )
                                }

                                ReleaseDownloadButtons(
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    onInteractiveClicked = {
                                        onNavigateToSeriesRelease(currentEpisode.id)
                                    },
                                    onAutomaticClicked = {
                                        viewModel.executeAutomaticSearch()
                                    },
                                    automaticSearchEnabled = currentEpisode.monitored,
                                )

                                if (queueItems.isNotEmpty()) {
                                    MediaActivitySection(
                                        queueItems = queueItems,
                                        onQueueItemClicked = { selectedQueueItem = it },
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                    )
                                }

                                Text(
                                    text = mokoString(MR.strings.files),
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                )
                                currentEpisode.episodeFile?.let { file ->
                                    FileCard(file, modifier = Modifier.padding(horizontal = 24.dp))
                                } ?: run {
                                    Text(
                                        text = mokoString(MR.strings.no_files),
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp),
                                    )
                                }

                                series.id?.let { seriesId ->
                                    BazarrSubtitlesSection(
                                        target =
                                        BazarrMediaTarget.Episode(
                                            seriesId,
                                            currentEpisode.id,
                                        ),
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                    )
                                }

                                when (val historyResult = history) {
                                    is HistoryState.Loading -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            LoadingIndicator()
                                        }
                                    }

                                    is HistoryState.Success -> {
                                        Text(
                                            text = mokoString(MR.strings.history),
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.padding(horizontal = 24.dp),
                                        )
                                        if (historyResult.items.isEmpty()) {
                                            Text(
                                                text = mokoString(MR.strings.no_history),
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center,
                                                modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 24.dp),
                                            )
                                        } else {
                                            historyResult.items.forEach { historyItem ->
                                                HistoryItemView(
                                                    item = historyItem,
                                                    modifier = Modifier.padding(horizontal = 24.dp),
                                                )
                                            }
                                        }
                                    }

                                    is HistoryState.Error -> {}
                                    else -> {}
                                }
                            }
                        }

                        EpisodeDetailsTab.Analytics -> {
                            TracearrAnalyticsSection(
                                uiState = tracearrState,
                                onWindowSelected = { viewModel.selectTracearrStatsWindow(it) },
                                modifier = Modifier.padding(horizontal = 24.dp),
                            )
                        }

                        EpisodeDetailsTab.Streams -> {
                            TracearrHistorySection(
                                uiState = tracearrState,
                                onLoadMore = { viewModel.loadMoreTracearrHistory() },
                                onClickItem = { selectedTracearrStreamSession = it.toStreamSession() },
                                onClickActiveStream = { selectedTracearrStreamSession = it },
                                isLargeScreen = isExpanded,
                                modifier = Modifier.padding(horizontal = 24.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp + LocalFloatingBarBottomPadding.current))
                }
            }
        }

        if (confirmDelete) {
            ConfirmDeleteEpisodeDialog(
                onConfirm = {
                    confirmDelete = false
                    viewModel.deleteEpisode()
                },
                onDismiss = { confirmDelete = false },
            )
        }

        selectedQueueItem?.let { item ->
            QueueItemInfoSheet(
                item = item,
                onDismiss = { selectedQueueItem = null },
                onRemove = { showConfirmRemoveQueueItem = true },
            )
        }

        if (showConfirmRemoveQueueItem && selectedQueueItem != null) {
            ConfirmDeleteItemSheet(
                onDismiss = { showConfirmRemoveQueueItem = false },
                deleteInProgress = removeQueueItemStatus is OperationStatus.InProgress,
                onDelete = { clientRemove, blocklist, skipRedownload ->
                    viewModel.removeQueueItem(
                        queueItem = selectedQueueItem!!,
                        removeFromClient = clientRemove,
                        addToBlocklist = blocklist,
                        skipRedownload = skipRedownload,
                    )
                    showConfirmRemoveQueueItem = false
                    selectedQueueItem = null
                },
            )
        }

        selectedTracearrStreamSession?.let { session ->
            TracearrStreamDetailsSheet(
                session = session,
                onDismissRequest = { selectedTracearrStreamSession = null },
                onNavigateToDetails = { _, _ -> },
                onNavigateToUser = { /* user profile */ },
            )
        }
    }
}
