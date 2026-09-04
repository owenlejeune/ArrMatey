package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.AudiobookFile
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.viewmodel.AudiobookFilesViewModel
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.HistoryItemView
import com.dnfapps.arrmatey.ui.components.MediaActivitySection
import com.dnfapps.arrmatey.ui.tabs.ConfirmDeleteItemSheet
import com.dnfapps.arrmatey.ui.tabs.QueueItemInfoSheet
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookFilesScreen(
    audiobook: Audiobook,
    onBack: () -> Unit = {},
    viewModel: AudiobookFilesViewModel =
        koinViewModel(key = (audiobook.id ?: 0L).toString(), parameters = { parametersOf(audiobook.id ?: 0L) }),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val queueItems by viewModel.queueItems.collectAsStateWithLifecycle()
    val removeQueueItemStatus by viewModel.removeQueueItemStatus.collectAsStateWithLifecycle()

    var selectedQueueItem by remember { mutableStateOf<QueueItem?>(null) }
    var showConfirmRemoveQueueItem by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshHistory() },
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (queueItems.isNotEmpty()) {
                    item {
                        MediaActivitySection(
                            queueItems = queueItems,
                            onQueueItemClicked = { selectedQueueItem = it },
                        )
                    }
                }
                item {
                    Text(
                        text = mokoString(MR.strings.files),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                items(uiState.files) { file ->
                    AudiobookFileCard(file)
                }
                item {
                    Text(
                        text = mokoString(MR.strings.history),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                items(uiState.history) { historyItem ->
                    HistoryItemView(historyItem)
                }
                if (uiState.history.isEmpty()) {
                    item {
                        Text(mokoString(MR.strings.no_history))
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
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
    }
}

@Composable
fun AudiobookFileCard(file: AudiobookFile) {
    ContainerCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text =
                file.path
                    ?.split("/")
                    ?.lastOrNull()
                    ?.breakable()
                    ?: mokoString(MR.strings.unknown),
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text =
                listOfNotNull(
                    file.format,
                    file.size?.bytesAsFileSizeString(),
                ).joinToString(BULLET),
            fontSize = 12.sp,
        )
        file.createdAt?.format("MMM d, yyyy")?.let { formattedDate ->
            Text(
                text = mokoString(MR.strings.added_on, formattedDate),
                fontSize = 12.sp,
            )
        }
    }
}
