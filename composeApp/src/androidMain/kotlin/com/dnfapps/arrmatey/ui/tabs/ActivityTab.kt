package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.LidarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.QueueDownloadState
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.RadarrQueueItem
import com.dnfapps.arrmatey.arr.api.model.SonarrQueueItem
import com.dnfapps.arrmatey.arr.viewmodel.ActivityQueueViewModel
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.bullet
import com.dnfapps.arrmatey.isDebug
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.menu.ActivityFilterMenu
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun ActivityTab(
    viewModel: ActivityQueueViewModel = koinInject()
) {
    val queueItems by viewModel.queueItems.collectAsStateWithLifecycle()
    val instances by viewModel.instances.collectAsStateWithLifecycle()
    val uiState by viewModel.activityQueueUiState.collectAsStateWithLifecycle()
    val removeItemStatus by viewModel.removeItemState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isPolling.collectAsStateWithLifecycle()

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
                            onSortOrderChanged = { viewModel.setSortOrder(it)}
                        )
                    }
                },
                navigationIcon = {
                    NavigationDrawerButton()
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() }
        ) {
            if (queueItems.isEmpty()) {
                EmptyActivityState(modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxSize()
                ) {
                    items(items = queueItems) { item ->
                        ActivityItem(item) {
                            selectedItem = item
                        }
                    }
                    item {
                        Spacer(Modifier.height(0.dp))
                    }
                }
            }

            selectedItem?.let { item ->
                QueueItemInfoSheet(
                    item = item,
                    onDismiss = { selectedItem = null },
                    onRemove = { showConfirmRemove = true }
                )
            }

            if (showConfirmRemove && selectedItem != null) {
                ConfirmDeleteItemSheet(
                    onDismiss = { showConfirmRemove = false },
                    deleteInProgress = removeItemStatus is OperationStatus.InProgress,
                    onDelete = { clientRemove, blocklist, skipRedownload ->
                        viewModel.removeQueueItem(selectedItem!!, clientRemove, blocklist, skipRedownload)
                    }
                )
            }
        }
    }
}

@Composable
fun ActivityItem(
    item: QueueItem,
    onClick: () -> Unit
) {
    val colors = when {
        item.hasIssue -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
        item is SonarrQueueItem -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        item is RadarrQueueItem -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        item is LidarrQueueItem -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> CardDefaults.cardColors()
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = colors
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.titleLabel,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val statusRow = buildString {
                    append(item.statusLabel)
                    if (item.trackedDownloadState == QueueDownloadState.Downloading) {
                        bullet()
                        append(item.progressLabel)
                        item.remainingTimeLabel?.let { remainingTimeLabel ->
                            bullet()
                            append(remainingTimeLabel)
                            append(" left")
                        }
                    }
                }
                Text(
                    text = statusRow,
                    fontSize = 14.sp
                )

                Text(
                    text = item.instanceName ?: "",
                    fontSize = 12.sp
                )
            }

            if (item.hasIssue) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun QueueItemInfoSheet(
    onDismiss: () -> Unit,
    onRemove: () -> Unit,
    item: QueueItem
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = item.titleLabel,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = item.title ?: mokoString(MR.strings.unknown),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            val statusRow = buildAnnotatedString {
                withStyle(SpanStyle(
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Medium
                )) {
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
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$remainingTime left",
                            fontSize = 12.sp
                        )
                        Text(
                            text = item.progressLabel,
                            fontSize = 12.sp
                        )
                    }
                    LinearProgressIndicator(
                        progress = { item.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            val chipItems = listOfNotNull(
                item.scoreLabel
            ) + item.customFormats.map { it.name }
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                chipItems.forEach { chipItem ->
                    Box(
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp))
                    ) {
                        Text(chipItem,
                            modifier = Modifier.padding(vertical = 2.dp, horizontal = 6.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item.errorMessage?.let { errorMessage ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(errorMessage, modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ))
                }
            } ?: item.statusMessages.forEach { status ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = status.title ?: "",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        status.messages.forEach { message ->
                            Text(
                                text = message,
                                fontSize = 14.sp,
                                lineHeight = 16.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }

            val infoItems = mapOf(
                MR.strings.protocol to item.protocol.name,
                MR.strings.download_client to item.downloadClient,
                MR.strings.indexer to item.indexer,
                MR.strings.languages to item.languageLabels.takeUnless { it.isEmpty() }?.joinToString(", "),
                MR.strings.added to item.added?.format(),
                MR.strings.destination to item.outputPath
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                modifier = Modifier.heightIn(max = 1000.dp),
                userScrollEnabled = false
            ) {
                infoItems.forEach { (key, value) ->
                    value?.let {
                        item {
                            Text(text = mokoString(key), fontWeight = FontWeight.SemiBold)
                        }
                        item {
                            Text(text = value, fontSize = 14.sp)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRemove,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                    Text(
                        text = mokoString(MR.strings.remove)
                    )
                }

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (isDebug() && item.needsManualImport) {
                        Button(
                            onClick = {
                                // todo
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null
                            )
                            Text(
                                text = mokoString(MR.strings.manual_import)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyActivityState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = mokoString(MR.strings.no_activity),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteItemSheet(
    onDismiss: () -> Unit,
    deleteInProgress: Boolean,
    onDelete: (Boolean, Boolean, Boolean) -> Unit
) {
    var removeFromClient by remember { mutableStateOf(false) }
    var blocklistRelease by remember { mutableStateOf(false) }
    var skipRedownload by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.client_remove_title),
                sublabel = mokoString(MR.strings.client_remove_message),
                checked = removeFromClient,
                onCheckedChange = { removeFromClient = it }
            )
            LabelledSwitch(
                label = mokoString(MR.strings.blocklist_title),
                sublabel = mokoString(MR.strings.blocklist_message),
                checked = blocklistRelease,
                onCheckedChange = { blocklistRelease = it }
            )
            if (blocklistRelease) {
                LabelledSwitch(
                    label = mokoString(MR.strings.skip_redownload_title),
                    sublabel = mokoString(MR.strings.skip_redownload_message),
                    checked = skipRedownload,
                    onCheckedChange = { skipRedownload = it }
                )
            }
            Button(
                onClick = {
                    onDelete(removeFromClient, blocklistRelease, blocklistRelease && skipRedownload)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                enabled = !deleteInProgress
            ) {
                if (deleteInProgress) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                    Text(
                        text = mokoString(MR.strings.remove)
                    )
                }
            }
        }
    }
}