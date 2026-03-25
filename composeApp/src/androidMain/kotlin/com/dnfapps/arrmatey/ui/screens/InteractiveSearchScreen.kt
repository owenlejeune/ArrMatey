package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.state.DownloadState
import com.dnfapps.arrmatey.arr.state.ReleaseLibrary
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.arr.viewmodel.InteractiveSearchViewModel
import com.dnfapps.arrmatey.compose.utils.ReleaseFilterBy
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.compose.utils.singleLanguageLabel
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.entensions.bullet
import com.dnfapps.arrmatey.extensions.formatAgeMinutes
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.ErrorView
import com.dnfapps.arrmatey.ui.components.ProgressBox
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.ui.menu.InteractiveSearchMenu
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InteractiveSearchScreen(
    instanceType: InstanceType,
    releaseParams: ReleaseParams,
    defaultFilter: ReleaseFilterBy = ReleaseFilterBy.Any,
    viewModel: InteractiveSearchViewModel = koinInjectParams(instanceType, defaultFilter),
    instanceViewModel: InstancesViewModel = koinInjectParams(instanceType),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.arr(instanceType)
) {
    val context = LocalContext.current
    val releaseUiState by viewModel.releaseUiState.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadReleaseState.collectAsStateWithLifecycle()
    val downloadStatus by viewModel.downloadStatus.collectAsStateWithLifecycle()
    val filterState by viewModel.filterUiState.collectAsStateWithLifecycle()

    val textFieldState = rememberTextFieldState()
    var confirmRelease by remember { mutableStateOf<ArrRelease?>( null) }

    val downloadQueueSuccessMessage = mokoString(MR.strings.download_queue_success)
    val downloadQueueErrorMessage = mokoString(MR.strings.download_queue_error)

    val instanceState by instanceViewModel.instancesState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        snapshotFlow { textFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { query ->
                viewModel.updateSearchQuery(query)
            }
    }

    LaunchedEffect(releaseParams) {
        viewModel.getRelease(releaseParams)
    }

    LaunchedEffect(downloadStatus) {
        when (downloadStatus) {
            true -> Toast.makeText(context, downloadQueueSuccessMessage, Toast.LENGTH_SHORT).show()
            false -> Toast.makeText(context, downloadQueueErrorMessage, Toast.LENGTH_SHORT).show()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            ArrAppBarWithSearch(
                textFieldState = textFieldState,
                navigationIcon =  { BackButton(navigation) },
                actions = {
                    InteractiveSearchMenu(
                        type = instanceType,
                        selectedSortBy = filterState.sortBy,
                        onSortByChanged = { viewModel.setSortBy(it) },
                        selectedSortOrder = filterState.sortOrder,
                        onSortOrderChanged = { viewModel.setSortOrder(it) },
                        selectedFilter = filterState.filterBy,
                        onFilterChanged = { viewModel.setFilterBy(it) },
                        libraryState = (releaseUiState as? ReleaseLibrary.Success),
                        filterLanguage = filterState.language,
                        onLanguageChange = { viewModel.setFilterLanguage(it) },
                        filterCustomFormat = filterState.customFormat,
                        onCustomFormatChange = { viewModel.setFilterCustomFormat(it) },
                        filterQualityInfo = filterState.quality,
                        onQualityChange = { viewModel.setFilterQuality(it) },
                        filterIndexer = filterState.indexer,
                        onIndexerChange = { viewModel.setFilterIndexer(it) },
                        filterProtocol = filterState.protocol,
                        onProtocolChange = { viewModel.setFilterProtocol(it) }
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (val state = releaseUiState) {
                is ReleaseLibrary.Loading -> {
                    LoadingIndicator(
                        modifier = Modifier.size(96.dp)
                    )
                }
                is ReleaseLibrary.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(state.items) { item ->
                            val shouldAnimate =
                                (downloadState as? DownloadState.Loading)?.guid == item.guid
                            ReleaseItem(
                                item = item,
                                onItemClick = {
                                    if (item.downloadAllowed) {
                                        viewModel.downloadRelease(item)
                                    } else {
                                        confirmRelease = item
                                    }
                                },
                                animate = shouldAnimate
                            )
                        }
                        if (state.items.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mokoString(MR.strings.no_results_found)
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(0.dp))
                        }
                    }
                }
                is ReleaseLibrary.Error -> {
                    ErrorView(
                        errorType = state.type,
                        message = state.message,
                        onOpenSettings = {
                            instanceState.selectedInstance?.let {
                                navigationManager.openEditInstanceScreen(it.id)
                            }
                        },
                        onRetry = { viewModel.getRelease(releaseParams) }
                    )
                }
                else -> {}
            }

            confirmRelease?.let { release ->
                AlertDialog(
                    onDismissRequest = {
                        confirmRelease = null
                    },
                    title = {
                        Text(mokoString(MR.strings.grab_release_title))
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(mokoString(MR.strings.grab_release_message))
                            ReleaseItem(release)
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.downloadRelease(release, force = true)
                                confirmRelease = null
                            }
                        ) {
                            Text(mokoString(MR.strings.grab))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick =  {
                                confirmRelease = null
                            }
                        ) {
                            Text(mokoString(MR.strings.cancel))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun <T: ArrRelease> ReleaseItem(
    item: T,
    onItemClick: ((T) -> Unit)? = null,
    animate: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onItemClick?.invoke(item) },
                enabled = onItemClick != null
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        ProgressBox(
            animate = animate
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val secondLine = buildAnnotatedString {
                    withStyle(SpanStyle(color = item.peerColor)) {
                        append(item.typeLabel)
                    }
                    bullet()
                    item.quality?.qualityLabel?.let { qualityLabel ->
                        append(qualityLabel)
                        bullet()
                    }
                    append(item.size.bytesAsFileSizeString())
                }
                Text(
                    text = secondLine,
                    maxLines = 1
                )

                val thirdLine = listOf(
                    item.languages.singleLanguageLabel(),
                    item.indexerLabel,
                    item.ageMinutes.formatAgeMinutes()
                ).joinToString(Bullet)
                Text(
                    text = thirdLine,
                    maxLines = 1
                )
            }
        }
    }
}