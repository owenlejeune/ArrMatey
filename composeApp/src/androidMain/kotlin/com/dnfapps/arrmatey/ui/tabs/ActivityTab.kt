package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.QueueDownloadState
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.bullet
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.menu.ActivityFilterMenu
import com.dnfapps.arrmatey.ui.theme.surfaceDark
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalTime::class)
@Composable
fun ActivityTab(
    wideRailIsVisible: Boolean,
    viewModel: ActivityQueueViewModel = koinViewModel(),
    preferences: PreferencesStore = koinInject(),
) {
    val queueItems by viewModel.queueItems.collectAsStateWithLifecycle()
    val instances by viewModel.instances.collectAsStateWithLifecycle()
    val uiState by viewModel.activityQueueUiState.collectAsStateWithLifecycle()
    val removeItemStatus by viewModel.removeItemState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isPolling.collectAsStateWithLifecycle()
    val hasLoaded by viewModel.hasLoaded.collectAsStateWithLifecycle()
    val useColoredCards by preferences.useColoredActivityCards.collectAsStateWithLifecycle(false)

    var showConfirmRemove by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<QueueItem?>(null) }

    LaunchedEffect(removeItemStatus) {
        if (removeItemStatus is OperationStatus.Success) {
            selectedItem = null
            showConfirmRemove = false
            viewModel.refresh()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    val countText = if (queueItems.isNotEmpty()) " (${queueItems.size})" else ""
                    Text(mokoString(MR.strings.activity) + countText)
                },
                actions = {
                    if (queueItems.isNotEmpty()) {
                        ActivityFilterMenu(
                            instances,
                            selectedInstanceId = uiState.instanceId,
                            onInstanceChange = { viewModel.setInstanceId(it) },
                            sortBy = uiState.sortBy,
                            onSortByChanged = { viewModel.setSortBy(it) },
                            sortOrder = uiState.sortOrder,
                            onSortOrderChanged = { viewModel.setSortOrder(it) },
                        )
                    }
                },
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (instances.isNotEmpty() && (!hasLoaded || (queueItems.isEmpty() && isLoading))) {
                LoadingIndicator(
                    modifier = Modifier.size(96.dp),
                )
            } else {
                PullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                    isRefreshing = isLoading,
                    onRefresh = { viewModel.refresh() },
                ) {
                    if (queueItems.isEmpty()) {
                        EmptyActivityState(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier =
                                Modifier
                                    .padding(horizontal = 12.dp)
                                    .fillMaxSize(),
                        ) {
                            items(items = queueItems) { item ->
                                ActivityItem(
                                    item = item,
                                    useFullColorCards = useColoredCards,
                                ) {
                                    selectedItem = item
                                }
                            }
                            item {
                                Spacer(Modifier.height(LocalFloatingBarBottomPadding.current + 16.dp))
                            }
                        }
                    }
                }
            }

            selectedItem?.let { item ->
                QueueItemInfoSheet(
                    item = item,
                    onDismiss = { selectedItem = null },
                    onRemove = { showConfirmRemove = true },
                )
            }

            if (showConfirmRemove && selectedItem != null) {
                ConfirmDeleteItemSheet(
                    onDismiss = { showConfirmRemove = false },
                    deleteInProgress = removeItemStatus is OperationStatus.InProgress,
                    onDelete = { clientRemove, blocklist, skipRedownload ->
                        viewModel.removeQueueItem(selectedItem!!, clientRemove, blocklist, skipRedownload)
                    },
                )
            }
        }
    }
}

@Composable
fun ActivityItem(
    item: QueueItem,
    useFullColorCards: Boolean = false,
    onClick: () -> Unit,
) {
    val containerColor =
        when {
            item.hasIssue -> MaterialTheme.colorScheme.errorContainer
            useFullColorCards -> item.type.associatedColor
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        }
    val contentColor =
        when {
            item.hasIssue -> MaterialTheme.colorScheme.onErrorContainer
            useFullColorCards -> surfaceDark
            else -> MaterialTheme.colorScheme.onSurface
        }
    val secondaryContentColor =
        when {
            item.hasIssue -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            useFullColorCards -> surfaceDark.copy(alpha = 0.8f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!useFullColorCards) {
                Box(
                    modifier =
                        Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(item.type.associatedColor),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f).padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.titleLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            overflow = TextOverflow.Ellipsis,
                            color = contentColor,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (item.hasIssue) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = if (useFullColorCards) surfaceDark else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp).padding(start = 4.dp),
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        item.instanceName?.takeIf { it.isNotBlank() }?.let { instanceName ->
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = if (useFullColorCards) surfaceDark.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                            ) {
                                Text(
                                    text = instanceName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondaryContentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }

                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color =
                                if (item.hasIssue) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                } else if (useFullColorCards) {
                                    surfaceDark.copy(alpha = 0.15f)
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                },
                        ) {
                            Text(
                                text = item.statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color =
                                    if (item.hasIssue) {
                                        MaterialTheme.colorScheme.error
                                    } else if (useFullColorCards) {
                                        surfaceDark
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }

                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = if (useFullColorCards) surfaceDark.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                        ) {
                            Text(
                                text = item.quality.qualityLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = secondaryContentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }

                        if (item.size > 0f) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = if (useFullColorCards) surfaceDark.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                            ) {
                                Text(
                                    text = item.size.toLong().bytesAsFileSizeString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondaryContentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }

                        if (item.trackedDownloadState == QueueDownloadState.Downloading) {
                            item.remainingTimeLabel?.let { remainingTimeLabel ->
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = if (useFullColorCards) surfaceDark.copy(alpha = 0.15f) else MaterialTheme.colorScheme.tertiaryContainer,
                                ) {
                                    Text(
                                        text = "$remainingTimeLabel left",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (useFullColorCards) surfaceDark else MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                    }

                    if (item.trackedDownloadState == QueueDownloadState.Downloading && item.progressPercent > 0f) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { item.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                strokeCap = StrokeCap.Round,
                                color = if (useFullColorCards) surfaceDark else MaterialTheme.colorScheme.primary,
                                trackColor = if (useFullColorCards) surfaceDark.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                Text(
                                    text = item.progressLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = secondaryContentColor,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun QueueItemInfoSheet(
    onDismiss: () -> Unit,
    onRemove: () -> Unit,
    item: QueueItem,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
        ) {
            Text(
                text = item.titleLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = item.title ?: mokoString(MR.strings.unknown),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            val statusRow =
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium,
                        ),
                    ) {
                        append(item.statusLabel)
                    }
                    bullet()
                    append(item.quality.qualityLabel)
                    bullet()
                    append(item.size.toLong().bytesAsFileSizeString())
                }

            Text(text = statusRow, modifier = Modifier.padding(vertical = 4.dp))

            item.remainingTimeLabel?.let { remainingTime ->
                Column(
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "$remainingTime left",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = item.progressLabel,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { item.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round,
                    )
                }
            }

            val chipItems =
                listOfNotNull(
                    item.scoreLabel,
                ) + item.customFormats.map { it.name }
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                chipItems.forEach { chipItem ->
                    Box(
                        modifier =
                            Modifier.border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = MaterialTheme.shapes.small,
                            ),
                    ) {
                        Text(
                            chipItem,
                            modifier = Modifier.padding(vertical = 2.dp, horizontal = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            item.errorMessage?.let { errorMessage ->
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Text(
                        errorMessage,
                        modifier =
                            Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp,
                            ),
                    )
                }
            } ?: item.statusMessages.forEach { status ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    ) {
                        Text(
                            text = status.title ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        status.messages.forEach { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            )
                        }
                    }
                }
            }

            val infoItems =
                mapOf(
                    MR.strings.protocol to item.protocol.name,
                    MR.strings.download_client to item.downloadClient,
                    MR.strings.indexer to item.indexer,
                    MR.strings.languages to item.languageLabels.takeUnless { it.isEmpty() }?.joinToString(", "),
                    MR.strings.added to item.added?.format(),
                    MR.strings.destination to item.outputPath,
                )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                modifier = Modifier.heightIn(max = 1000.dp),
                userScrollEnabled = false,
            ) {
                infoItems.forEach { (key, value) ->
                    value?.let {
                        item {
                            Text(
                                text = mokoString(key),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        item {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onRemove,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                    Text(
                        text = mokoString(MR.strings.remove),
                    )
                }

                Box(
                    modifier = Modifier.weight(1f),
                ) {
                    if (isDebug() && item.needsManualImport) {
                        Button(
                            onClick = {
                                // todo
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                            )
                            Text(
                                text = mokoString(MR.strings.manual_import),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyActivityState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = mokoString(MR.strings.no_activity),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteItemSheet(
    onDismiss: () -> Unit,
    deleteInProgress: Boolean,
    onDelete: (Boolean, Boolean, Boolean) -> Unit,
) {
    var removeFromClient by remember { mutableStateOf(false) }
    var blocklistRelease by remember { mutableStateOf(false) }
    var skipRedownload by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = {
            if (!deleteInProgress) {
                onDismiss()
            }
        },
        sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { !deleteInProgress },
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.client_remove_title),
                sublabel = mokoString(MR.strings.client_remove_message),
                checked = removeFromClient,
                onCheckedChange = { removeFromClient = it },
            )
            LabelledSwitch(
                label = mokoString(MR.strings.blocklist_title),
                sublabel = mokoString(MR.strings.blocklist_message),
                checked = blocklistRelease,
                onCheckedChange = { blocklistRelease = it },
            )
            if (blocklistRelease) {
                LabelledSwitch(
                    label = mokoString(MR.strings.skip_redownload_title),
                    sublabel = mokoString(MR.strings.skip_redownload_message),
                    checked = skipRedownload,
                    onCheckedChange = { skipRedownload = it },
                )
            }
            Button(
                onClick = {
                    onDelete(removeFromClient, blocklistRelease, blocklistRelease && skipRedownload)
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                enabled = !deleteInProgress,
            ) {
                if (deleteInProgress) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                    Text(
                        text = mokoString(MR.strings.remove),
                    )
                }
            }
        }
    }
}
