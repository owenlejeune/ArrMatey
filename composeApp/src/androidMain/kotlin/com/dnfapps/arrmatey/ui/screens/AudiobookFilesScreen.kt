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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.AudiobookFile
import com.dnfapps.arrmatey.arr.viewmodel.AudiobookFilesViewModel
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.components.ExtraFileCard
import com.dnfapps.arrmatey.ui.components.FileCard
import com.dnfapps.arrmatey.ui.components.HistoryItemView
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookFilesScreen(
    audiobook: Audiobook,
    viewModel: AudiobookFilesViewModel = koinInjectParams(audiobook.id ?: 0L),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.audiobooks()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { navigation.popBackStack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refreshHistory() },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = mokoString(MR.strings.files),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                items(uiState.files) { file ->
                    AudiobookFileCard(file)
                }
                item {
                    Text(
                        text = mokoString(MR.strings.history),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
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
    }
}

@Composable
fun AudiobookFileCard(file: AudiobookFile) {
    ContainerCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = file.path?.split("/")?.lastOrNull()?.breakable()
                ?: mokoString(MR.strings.unknown),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = listOfNotNull(
                file.format,
                file.size?.bytesAsFileSizeString()
            ).joinToString(Bullet),
            fontSize = 12.sp
        )
        file.createdAt?.format("MMM d, yyyy")?.let { formattedDate ->
            Text(
                text = mokoString(MR.strings.added_on, formattedDate),
                fontSize = 12.sp
            )
        }
    }
}