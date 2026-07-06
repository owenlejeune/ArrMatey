package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.state.MediaPreviewUiState
import com.dnfapps.arrmatey.arr.viewmodel.MediaPreviewViewModel
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.arrNavigator
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DetailsHeader
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.UpcomingDateView
import com.dnfapps.arrmatey.ui.sheets.AddArtistSheet
import com.dnfapps.arrmatey.ui.sheets.AddAudiobookSheet
import com.dnfapps.arrmatey.ui.sheets.AddAuthorSheet
import com.dnfapps.arrmatey.ui.sheets.AddMovieSheet
import com.dnfapps.arrmatey.ui.sheets.AddSeriesSheet
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPreviewScreen(
    item: ArrMedia,
    type: InstanceType,
    isExpanded: Boolean = false,
    viewModel: MediaPreviewViewModel = koinInjectParams(item, type)
) {
    val navigation = arrNavigator
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val successMessage = mokoString(MR.strings.success)

    LaunchedEffect(uiState.addItemStatus) {
        when (val status = uiState.addItemStatus) {
            is OperationStatus.Success -> {
                showBottomSheet = false
                Toast.makeText(context, status.message ?: successMessage, Toast.LENGTH_SHORT).show()
            }
            is OperationStatus.Error -> {
                status.message?.let { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
    }

    LaunchedEffect(uiState.lastAddedItemId) {
        uiState.lastAddedItemId?.let { id ->
            showBottomSheet = false
            navigation.replaceBackStack(listOf(ArrScreen.Library, ArrScreen.Details(id)))
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
                            imageVector = if (isExpanded) Icons.Default.Close else Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(if (isExpanded) MR.strings.close else MR.strings.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showBottomSheet = true },
                        colors = IconButtonDefaults.headerBarColors()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues.copy(bottom = 0.dp, top = 0.dp))
            .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailsHeader(item, type, topPadding = paddingValues.calculateTopPadding())

                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    UpcomingDateView(item)

                    item.overview?.let { overview ->
                        ItemDescriptionCard(overview)
                    }
                }
            }

            if (showBottomSheet) {
                AddMediaSheet(
                    item = item,
                    uiState = uiState,
                    onAddItem = { newItem, searchOnAdd ->
                        viewModel.addItem(newItem, searchOnAdd)
                    },
                    onUpdatePreferences = viewModel::updatePreferences,
                    onDismiss = { showBottomSheet = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMediaSheet(
    item: ArrMedia,
    uiState: MediaPreviewUiState,
    onAddItem: (ArrMedia, Boolean) -> Unit,
    onUpdatePreferences: (InstancePreferences) -> Unit,
    onDismiss: () -> Unit
) {
    when (item) {
        is ArrSeries -> AddSeriesSheet(
            item,
            uiState.qualityProfiles,
            uiState.rootFolders,
            uiState.tags,
            uiState.addItemStatus == OperationStatus.InProgress,
            uiState.preferences,
            onUpdatePreferences,
            onAddItem,
            onDismiss
        )
        is ArrMovie -> AddMovieSheet(
            item,
            uiState.qualityProfiles,
            uiState.rootFolders,
            uiState.tags,
            uiState.addItemStatus == OperationStatus.InProgress,
            uiState.preferences,
            onUpdatePreferences,
            onAddItem,
            onDismiss
        )
        is Arrtist -> AddArtistSheet(
            item,
            uiState.qualityProfiles,
            uiState.rootFolders,
            uiState.tags,
            uiState.addItemStatus == OperationStatus.InProgress,
            uiState.preferences,
            onUpdatePreferences,
            onAddItem,
            onDismiss
        )
        is Author -> AddAuthorSheet(
            item,
            uiState.qualityProfiles,
            uiState.rootFolders,
            uiState.tags,
            uiState.addItemStatus == OperationStatus.InProgress,
            uiState.preferences,
            onUpdatePreferences,
            onAddItem,
            onDismiss
        )
        is SearchAudiobook -> AddAudiobookSheet(
            item,
            uiState.qualityProfiles,
            uiState.rootFolders,
            uiState.relativePath,
            uiState.addItemStatus == OperationStatus.InProgress,
            uiState.preferences,
            onUpdatePreferences,
            onAddItem,
            onDismiss
        )
        is Audiobook,
        is MockMedia -> {}
    }
}