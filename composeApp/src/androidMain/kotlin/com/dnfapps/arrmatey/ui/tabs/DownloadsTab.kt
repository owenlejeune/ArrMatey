package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.downloadclient.model.DownloadItem
import com.dnfapps.arrmatey.downloadclient.model.DownloadItemStatus
import com.dnfapps.arrmatey.downloadclient.state.DownloadClientCommandState
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadClientsViewModel
import com.dnfapps.arrmatey.downloadclient.viewmodel.DownloadQueueViewModel
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.menu.DownloadClientQueueSortMenu
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DownloadsTab(
    viewModel: DownloadQueueViewModel = koinInject(),
    clientsViewModel: DownloadClientsViewModel = koinInject()
) {
    val queueState by viewModel.downloadQueueState.collectAsStateWithLifecycle()
    val commandState by viewModel.commandState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val sortState by viewModel.sortState.collectAsStateWithLifecycle()
    val clientFiltersIds by viewModel.clientIdsFilters.collectAsStateWithLifecycle()

    val downloadClientState by clientsViewModel.downloadClientsState.collectAsStateWithLifecycle()

    var deleteTarget by remember { mutableStateOf<DownloadItem?>(null) }

    val textFieldState = rememberTextFieldState()

    LaunchedEffect(textFieldState.text) {
        viewModel.updateSearchQuery(textFieldState.text.toString())
    }

    LaunchedEffect(commandState) {
        if (commandState is DownloadClientCommandState.Success) {
            deleteTarget = null
            viewModel.resetCommandState()
        }
    }

    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            val count = queueState.queueItems.size
            val placeholderLabel = mokoString(MR.strings.search_downloads, count)

            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                searchPlaceholder = placeholderLabel,
                navigationIcon = {
                    NavigationDrawerButton()
                },
                actions = {
                    DownloadClientQueueSortMenu(
                        sortBy = sortState.sortBy,
                        onSortByChanged = { viewModel.updateSortBy(it) },
                        sortOrder = sortState.sortOrder,
                        onSortOrderChanged = { viewModel.updateSortOrder(it) }
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            contentAlignment = Alignment.Center
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Spacer(Modifier.width(18.dp))
                    downloadClientState.downloadClients.forEach { client ->
                        val info = queueState.transferInfo.firstOrNull { it.client.id == client.id }
                        FilterChip(
                            selected = downloadClientState.downloadClients.size > 1
                                    && clientFiltersIds.contains(client.id),
                            onClick = { viewModel.toggleClientIdFilter(client.id) },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(client.type.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "↓ ${(info?.downloadSpeed ?: 0).bytesAsFileSizeString()}/s  ↑ ${(info?.uploadSpeed ?: 0).bytesAsFileSizeString()}/s",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        )
                    }
                    Spacer(Modifier.width(18.dp))
                }
                if (queueState.queueItems.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = mokoString(MR.strings.no_activity))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        state = listState
                    ) {
                        items(items = queueState.queueItems, key = { it.id }) { item ->
                            DownloadQueueItem(
                                item = item,
                                actionInProgress = commandState is DownloadClientCommandState.Loading,
                                showClientInfo = clientFiltersIds.size > 1,
                                onPause = { viewModel.pauseDownload(item.id) },
                                onResume = { viewModel.resumeDownload(item.id) },
                                onDelete = { deleteTarget = item }
                            )
                        }
                    }
                }
            }
        }

        deleteTarget?.let { item ->
            DeleteDownloadDialog(
                commandState = commandState,
                onDismiss = { deleteTarget = null },
                onConfirm = { deleteFiles ->
                    viewModel.deleteDownload(item.id, deleteFiles)
                }
            )
        }
    }
}

@Composable
private fun DownloadQueueItem(
    item: DownloadItem,
    actionInProgress: Boolean,
    showClientInfo: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    if (item.status == DownloadItemStatus.Paused) {
                        IconButton(
                            onClick = onResume,
                            enabled = !actionInProgress
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        }
                    } else {
                        IconButton(
                            onClick = onPause,
                            enabled = !actionInProgress
                        ) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = null)
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        enabled = !actionInProgress
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    }
                }
            }

            val statusLabel = buildString {
                append(mokoString(item.status.resource))
                append(Bullet)
                append(item.downloadSpeed.bytesAsFileSizeString())
                append("/s")
            }
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LinearProgressIndicator(
                progress = { item.progress.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (item.etaString.isNotBlank()) {
                    Text(
                        text = "ETA: ${item.etaString}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (showClientInfo) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Image(
                            painter = painterResource(item.client.type.icon),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = item.client.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteDownloadDialog(
    commandState: DownloadClientCommandState,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    var deleteFiles by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(mokoString(MR.strings.confirm)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Remove this download?")
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = deleteFiles, onCheckedChange = { deleteFiles = it })
                    Text(mokoString(MR.strings.delete_files))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(deleteFiles) },
                enabled = commandState !is DownloadClientCommandState.Loading
            ) {
                Text(mokoString(MR.strings.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(mokoString(MR.strings.no))
            }
        }
    )
}

@Composable
private fun NoDownloadClientsView(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = koinInject()
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.CloudQueue,
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )
        Text(
            text = mokoString(MR.strings.no_download_clients),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                navigationManager.openNewDownloadClientScreen()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = mokoString(MR.strings.add_instance),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
