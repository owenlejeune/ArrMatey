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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.viewmodel.MediaPreviewViewModel
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.DetailsHeader
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.UpcomingDateView
import com.dnfapps.arrmatey.ui.sheets.AddArtistSheet
import com.dnfapps.arrmatey.ui.sheets.AddMovieSheet
import com.dnfapps.arrmatey.ui.sheets.AddSeriesSheet
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPreviewScreen(
    item: ArrMedia,
    type: InstanceType,
    viewModel: MediaPreviewViewModel = koinInjectParams(type),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<ArrScreen> = navigationManager.arr(type)
) {
    val context = LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val lastAddedItemId by viewModel.lastAddedItemId.collectAsStateWithLifecycle()
    val addItemStatus by viewModel.addItemStatus.collectAsStateWithLifecycle()
    val qualityProfiles by viewModel.qualityProfiles.collectAsStateWithLifecycle()
    val rootFolders by viewModel.rootFolders.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()

    val successMessage = mokoString(MR.strings.success)

    LaunchedEffect(addItemStatus) {
        when (val status = addItemStatus) {
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

    LaunchedEffect(lastAddedItemId) {
        lastAddedItemId?.let { id ->
            showBottomSheet = false
            val newScreen = ArrScreen.Details(id)
            navigation.replaceCurrent(newScreen)
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues.copy(bottom = 0.dp, top = 0.dp))
            .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailsHeader(item, type)

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    UpcomingDateView(item)

                    item.overview?.let { overview ->
                        ItemDescriptionCard(overview)
                    }
                }
            }

            OverlayTopAppBar(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.TopCenter),
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

            if (showBottomSheet) {
                AddMediaSheet(
                    item = item,
                    qualityProfiles = qualityProfiles,
                    rootFolders = rootFolders,
                    tags = tags,
                    addInProgress = addItemStatus is OperationStatus.InProgress,
                    onAddItem = { newItem, searchOnAdd ->
                        viewModel.addItem(newItem, searchOnAdd)
                    },
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
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    addInProgress: Boolean,
    onAddItem: (ArrMedia, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    when (item) {
        is ArrSeries -> AddSeriesSheet(
            item,
            qualityProfiles,
            rootFolders,
            tags,
            addInProgress,
            onAddItem,
            onDismiss
        )
        is ArrMovie -> AddMovieSheet(
            item,
            qualityProfiles,
            rootFolders,
            tags,
            addInProgress,
            onAddItem,
            onDismiss
        )
        is Arrtist -> AddArtistSheet(
            item,
            qualityProfiles,
            rootFolders,
            tags,
            addInProgress,
            onAddItem,
            onDismiss
        )
    }
}