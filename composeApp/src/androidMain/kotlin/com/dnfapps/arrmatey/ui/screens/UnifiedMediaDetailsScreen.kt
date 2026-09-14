package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.entensions.unlessEmpty
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsTab
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.tracearr.api.model.TracearrStreamSession
import com.dnfapps.arrmatey.ui.components.ConfirmDeleteAlert
import com.dnfapps.arrmatey.ui.components.InstancePicker
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.UnifiedDetailsHeader
import com.dnfapps.arrmatey.ui.components.tracearr.TracearrAnalyticsSection
import com.dnfapps.arrmatey.ui.components.tracearr.TracearrHistorySection
import com.dnfapps.arrmatey.ui.components.tracearr.TracearrSummaryChipRow
import com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs.ConfirmClearSeerrDataDialog
import com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs.ConfirmDeleteAlbumDialog
import com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs.ConfirmDeleteAudiobookDialog
import com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs.ConfirmDeleteEpisodeDialog
import com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs.ConfirmDeleteMovieDialog
import com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs.ConfirmDeleteSeasonDialog
import com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs.ConfirmDeleteSeerrFileDialog
import com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs.ConfirmMoveFilesDialog
import com.dnfapps.arrmatey.ui.components.unifiedmedia.dialogs.PendingSeerrRequestDialog
import com.dnfapps.arrmatey.ui.components.unifiedmedia.menus.MediaActionsToolbarMenus
import com.dnfapps.arrmatey.ui.components.unifiedmedia.menus.UnifiedMediaDetailsToolbarMenu
import com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets.AddMediaSheetsHost
import com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets.EditMediaSheetsHost
import com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets.SeerrReportIssueSheetHost
import com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets.SeerrRequestSheetHost
import com.dnfapps.arrmatey.ui.components.unifiedmedia.sheets.SeerrViewRequestSheetHost
import com.dnfapps.arrmatey.ui.components.unifiedmedia.tabs.OverviewTabContent
import com.dnfapps.arrmatey.ui.components.unifiedmedia.tabs.SeasonsFilesTabContent
import com.dnfapps.arrmatey.ui.helpers.LocalIsInTwoPane
import com.dnfapps.arrmatey.ui.screens.tracearr.TracearrStreamDetailsSheet
import com.dnfapps.arrmatey.ui.tabs.ConfirmDeleteItemSheet
import com.dnfapps.arrmatey.ui.tabs.QueueItemInfoSheet
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.handleWatchClick
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.viewmodel.UnifiedMediaDetailsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private typealias DetailsTab = UnifiedMediaDetailsTab

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnifiedMediaDetailsScreen(
    arrId: Long? = null,
    tmdbId: Long? = null,
    tvdbId: Long? = null,
    instanceType: InstanceType? = null,
    requestType: RequestType? = null,
    initialEpisodeId: Long? = null,
    isExpanded: Boolean = false,
    wideRailIsVisible: Boolean = false,
    onBack: () -> Unit,
    onNavigateToEpisodeDetails: (ArrSeries, Episode) -> Unit,
    onNavigateToSeriesRelease: (seriesId: Long?, seasonNumber: Int?, episodeId: Long?) -> Unit,
    onNavigateToMovieFiles: (ArrMovie) -> Unit,
    onNavigateToMovieReleases: (Long) -> Unit,
    onNavigateToAuthorFiles: (Author) -> Unit,
    onNavigateToBookDetails: (Author, Book) -> Unit,
    onNavigateToBookRelease: (Long) -> Unit,
    onNavigateToAudiobookFiles: (Audiobook) -> Unit,
    onNavigateToAudiobookRelease: (Long?, String?) -> Unit,
    onNavigateToAlbumRelease: (Long, Long) -> Unit,
    onPersonClick: (Long) -> Unit,
    instanceId: Long? = null,
    viewModel: UnifiedMediaDetailsViewModel =
        koinViewModel(key = "${arrId}_${tmdbId}_${tvdbId}_${instanceType}_${requestType}_$instanceId", parameters = {
            parametersOf(arrId, tmdbId, tvdbId, instanceType, requestType, instanceId)
        }),
    moko: MokoStrings = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var lastSuccessState by remember { mutableStateOf<UnifiedMediaDetailsUiState.Success?>(null) }
    var hasNavigatedToInitialEpisode by remember(initialEpisodeId) { mutableStateOf(false) }

    val successState = (uiState as? UnifiedMediaDetailsUiState.Success) ?: lastSuccessState

    LaunchedEffect(uiState) {
        if (uiState is UnifiedMediaDetailsUiState.Success) {
            lastSuccessState = uiState as UnifiedMediaDetailsUiState.Success
        }
    }

    LaunchedEffect(successState, initialEpisodeId, hasNavigatedToInitialEpisode) {
        if (initialEpisodeId != null && !hasNavigatedToInitialEpisode && successState != null) {
            val series =
                (successState.arrMedia as? ArrSeries)
                    ?: successState.episodes.firstNotNullOfOrNull { it.arrEpisode?.series }
            val episode = successState.episodes.mapNotNull { it.arrEpisode }.find { it.id == initialEpisodeId }

            if (series != null && episode != null) {
                hasNavigatedToInitialEpisode = true
                onNavigateToEpisodeDetails(series, episode)
            }
        }
    }

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val isDualPanel = LocalIsInTwoPane.current

    var confirmDelete by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showEditPathSheet by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var moveFilesItem by remember { mutableStateOf<ArrMedia?>(null) }
    var confirmDeleteSeasonNumber by remember { mutableStateOf<Int?>(null) }
    var confirmDeleteAlbum by remember { mutableStateOf<Long?>(null) }
    var confirmDeleteEpisodeId by remember { mutableStateOf<Long?>(null) }
    var confirmDeleteMovie by remember { mutableStateOf(false) }
    var confirmDeleteAudiobookFile by remember { mutableStateOf(false) }
    var editAlbum by remember { mutableStateOf<ArrAlbum?>(null) }
    var selectedQueueItem by remember { mutableStateOf<QueueItem?>(null) }
    var showConfirmRemoveQueueItem by remember { mutableStateOf(false) }
    var confirmRemoveFromService by remember { mutableStateOf(false) }
    var confirmClearData by remember { mutableStateOf(false) }
    var selectedTracearrStreamSession by remember { mutableStateOf<TracearrStreamSession?>(null) }
    var selectedTab by remember { mutableStateOf(DetailsTab.Overview) }
    var previousHasSeasonsOrFiles by remember { mutableStateOf<Boolean?>(null) }

    val qualityProfiles by viewModel.qualityProfiles.collectAsStateWithLifecycle()
    val rootFolders by viewModel.rootFolders.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val addItemStatus by viewModel.addItemStatus.collectAsStateWithLifecycle()
    val editStatus by viewModel.editStatus.collectAsStateWithLifecycle()
    val deleteStatus by viewModel.deleteStatus.collectAsStateWithLifecycle()
    val deleteSeasonStatus by viewModel.deleteSeasonStatus.collectAsStateWithLifecycle()
    val deleteAlbumStatus by viewModel.deleteAlbumStatus.collectAsStateWithLifecycle()
    val deleteMovieFileStatus by viewModel.deleteMovieFileStatus.collectAsStateWithLifecycle()
    val deleteAudiobookFileStatus by viewModel.deleteAudiobookFileStatus.collectAsStateWithLifecycle()
    val deleteEpisodeStatus by viewModel.deleteEpisodeStatus.collectAsStateWithLifecycle()
    val removeQueueItemStatus by viewModel.removeQueueItemStatus.collectAsStateWithLifecycle()
    val requestStatus by viewModel.requestStatus.collectAsStateWithLifecycle()
    val pendingSeerrRequest by viewModel.pendingSeerrRequest.collectAsStateWithLifecycle()

    val isRequestSheetVisible by viewModel.isRequestSheetVisible.collectAsStateWithLifecycle()
    val isReportIssueSheetVisible by viewModel.isReportIssueSheetVisible.collectAsStateWithLifecycle()
    val isViewRequestSheetVisible by viewModel.isViewRequestSheetVisible.collectAsStateWithLifecycle()
    val reportIssueState by viewModel.reportIssueState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val serviceDetails by viewModel.serviceDetails.collectAsStateWithLifecycle()
    val isArrConfigured by viewModel.isArrConfigured.collectAsStateWithLifecycle()
    val isSeerrConfigured by viewModel.isSeerrConfigured.collectAsStateWithLifecycle()
    val tracearrState by viewModel.tracearrState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val buttonState by viewModel.buttonState.collectAsStateWithLifecycle()
    val isMonitored by viewModel.isMonitored.collectAsStateWithLifecycle()

    val automaticSearchIds by viewModel.automaticSearchIds.collectAsStateWithLifecycle()
    val lastSearchResult by viewModel.lastSearchResult.collectAsStateWithLifecycle()
    val addSheetUiState by viewModel.addSheetUiState.collectAsStateWithLifecycle()
    val activeInstance by viewModel.activeInstance.collectAsStateWithLifecycle()
    val activeSeerrInstance by viewModel.activeSeerrInstance.collectAsStateWithLifecycle()

    val searchQueuedMessage = mokoString(MR.strings.search_queued)
    val searchErrorMessage = mokoString(MR.strings.search_error)
    val itemAddedSuccessfullyMessage = mokoString(MR.strings.item_added_successfully)
    val errorAddingItemMessage = mokoString(MR.strings.error_adding_item)
    val itemEditedSuccessfullyMessage = mokoString(MR.strings.item_edited_successfully)
    val errorEditingItemMessage = mokoString(MR.strings.error_editing_item)
    val itemDeletedSuccessfullyMessage = mokoString(MR.strings.item_deleted_successfully)
    val errorDeletingItemMessage = mokoString(MR.strings.error_deleting_item)

    LaunchedEffect(lastSearchResult) {
        when (lastSearchResult) {
            true -> Toast.makeText(context, searchQueuedMessage, Toast.LENGTH_SHORT).show()
            false -> Toast.makeText(context, searchErrorMessage, Toast.LENGTH_SHORT).show()
            else -> {}
        }
    }

    LaunchedEffect(addItemStatus) {
        when (addItemStatus) {
            is OperationStatus.Success -> {
                Toast.makeText(context, itemAddedSuccessfullyMessage, Toast.LENGTH_SHORT).show()
                showAddSheet = false
            }

            is OperationStatus.Error -> {
                Toast.makeText(context, errorAddingItemMessage, Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    LaunchedEffect(editStatus) {
        when (editStatus) {
            is OperationStatus.Success -> {
                Toast.makeText(context, itemEditedSuccessfullyMessage, Toast.LENGTH_SHORT).show()
                showEditSheet = false
                editAlbum = null
            }

            is OperationStatus.Error -> {
                Toast.makeText(context, errorEditingItemMessage, Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    LaunchedEffect(deleteStatus) {
        when (deleteStatus) {
            is OperationStatus.Success -> {
                Toast.makeText(context, itemDeletedSuccessfullyMessage, Toast.LENGTH_SHORT).show()
                onBack()
            }

            is OperationStatus.Error -> {
                Toast.makeText(context, errorDeletingItemMessage, Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    LaunchedEffect(deleteMovieFileStatus) {
        when (deleteMovieFileStatus) {
            is OperationStatus.Success ->
                Toast.makeText(context, itemDeletedSuccessfullyMessage, Toast.LENGTH_SHORT).show()

            is OperationStatus.Error ->
                Toast.makeText(context, errorDeletingItemMessage, Toast.LENGTH_SHORT).show()

            else -> {}
        }
    }

    LaunchedEffect(deleteAudiobookFileStatus) {
        when (deleteAudiobookFileStatus) {
            is OperationStatus.Success ->
                Toast.makeText(context, itemDeletedSuccessfullyMessage, Toast.LENGTH_SHORT).show()

            is OperationStatus.Error ->
                Toast.makeText(context, errorDeletingItemMessage, Toast.LENGTH_SHORT).show()

            else -> {}
        }
    }

    LaunchedEffect(removeQueueItemStatus) {
        when (removeQueueItemStatus) {
            is OperationStatus.Success -> {
                showConfirmRemoveQueueItem = false
                selectedQueueItem = null
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
                        onClick = { onBack() },
                        colors = IconButtonDefaults.headerBarColors(),
                    ) {
                        Icon(
                            imageVector = if (isDualPanel) Icons.Default.Close else Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(if (isDualPanel) MR.strings.close else MR.strings.back),
                        )
                    }
                },
                actions = {
                    val success = uiState as? UnifiedMediaDetailsUiState.Success ?: lastSuccessState
                    if (success != null) {
                        val showArrActions = success.hasArrId && isArrConfigured
                        val canAddDirectly = !success.hasArrId && success.arrMedia != null && isArrConfigured
                        val resolvedType = viewModel.resolvedInstanceType

                        AnimatedVisibility(
                            visible = buttonState.showReportIssueButton,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally(),
                        ) {
                            IconButton(
                                onClick = { viewModel.showReportIssueSheet() },
                                colors = IconButtonDefaults.headerBarColors(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = mokoString(MR.strings.report_issue),
                                    tint = ArrOrange,
                                )
                            }
                        }

                        MediaActionsToolbarMenus(
                            buttonState = buttonState,
                            canAddDirectly = canAddDirectly,
                            onWatchClicked = { url, provider ->
                                handleWatchClick(url, provider, context, moko)
                            },
                            onWatchTrailerClicked = { trailerUrl ->
                                context.openLink(trailerUrl)
                            },
                            onViewRequestClicked = { viewModel.showViewRequestSheet() },
                            onApproveRequestClicked = { viewModel.showViewRequestSheet() },
                            onDeclineRequestClicked = { viewModel.declineRequest(it) },
                            onRequestClicked = { viewModel.showRequestSheet(is4k = false) },
                            onRequest4kClicked = { viewModel.showRequestSheet(is4k = true) },
                            onAddDirectlyClicked = { showAddSheet = true },
                        )

                        if (showArrActions) {
                            IconButton(
                                onClick = { viewModel.toggleMonitored() },
                                colors = IconButtonDefaults.headerBarColors(),
                            ) {
                                AnimatedContent(
                                    targetState = isMonitored,
                                    label = "MonitoredIcon",
                                ) { monitored ->
                                    Icon(
                                        imageVector = if (monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = mokoString(if (monitored) MR.strings.unmonitored else MR.strings.monitored),
                                    )
                                }
                            }
                        }

                        if (resolvedType != null && success.availableInstances.size > 1) {
                            InstancePicker(
                                type = resolvedType,
                                currentInstance = success.availableInstances.firstOrNull { it.id == success.selectedInstanceId },
                                typeInstances = success.availableInstances,
                                onInstanceSelected = { viewModel.selectInstance(it.id) },
                                buttonColors = IconButtonDefaults.headerBarColors(),
                            )
                        }

                        val canDeleteFile = success.canDeleteFile(resolvedType)

                        UnifiedMediaDetailsToolbarMenu(
                            success = success,
                            buttonState = buttonState,
                            instanceType = resolvedType,
                            requestType = viewModel.resolvedRequestType ?: requestType,
                            isArrConfigured = isArrConfigured,
                            isSeerrConfigured = isSeerrConfigured,
                            isMonitored = isMonitored,
                            onRefresh = { viewModel.performRefresh() },
                            onAutomaticLookup = { viewModel.performAutomaticLookup() },
                            onAddMissingInstance = { missingInstance ->
                                viewModel.setAddSheetTargetInstance(missingInstance)
                                showAddSheet = true
                            },
                            onEdit = { showEditSheet = true },
                            onDelete = { confirmDelete = true },
                            onDeleteFile =
                                if (canDeleteFile) {
                                    {
                                        if (resolvedType == InstanceType.Radarr) {
                                            confirmDeleteMovie = true
                                        } else {
                                            confirmDeleteAudiobookFile = true
                                        }
                                    }
                                } else {
                                    null
                                },
                            onMarkAsAvailable = { viewModel.markSeerrMediaAsAvailable() },
                            onRemoveFromService = { confirmRemoveFromService = true },
                            onClearData = { confirmClearData = true },
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(paddingValues.copy(bottom = 0.dp, top = 0.dp))
                    .fillMaxSize(),
        ) {
            when {
                successState != null -> {
                    val state = successState
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = { viewModel.refresh() },
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            UnifiedDetailsHeader(
                                type = instanceType,
                                topPadding = paddingValues.calculateTopPadding(),
                                bannerUrl = state.bannerUrl,
                                posterUrl = state.posterUrl,
                                clearLogo = state.clearLogo,
                                ratings = state.ratings,
                                year = state.year,
                                runtimeString = state.runtimeString,
                                certification = state.getCertification(LocalLocale.current.platformLocale.country),
                                releasedBy = state.releasedBy,
                                seasonCount = state.seasonCount?.let { mokoPlural(MR.plurals.seasons, it) },
                                genres = state.genres,
                                isExpanded = isExpanded,
                                wideRailIsVisible = wideRailIsVisible,
                            )

                            Column(
                                modifier =
                                    Modifier
                                        .padding(bottom = 24.dp)
                                        .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp),
                            ) {
                                Column {
                                    val title = state.displayTitle ?: mokoString(MR.strings.unknown)
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                    )

                                    state.tagline?.unlessEmpty {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.padding(horizontal = 24.dp),
                                        )
                                    }

                                    state.upcomingDateString?.unlessEmpty { airingString ->
                                        Text(
                                            text = airingString,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 24.dp),
                                        )
                                    }

                                    if (state.isMovieOrTv) {
                                        TracearrSummaryChipRow(
                                            uiState = tracearrState,
                                            modifier = Modifier.padding(top = 8.dp),
                                        )
                                    }

                                    LaunchedEffect(state.hasSeasonsOrFiles) {
                                        val prev = previousHasSeasonsOrFiles
                                        if (prev == null) {
                                            if (state.hasSeasonsOrFiles) {
                                                selectedTab = DetailsTab.SeasonsFiles
                                            }
                                        } else if (!prev && state.hasSeasonsOrFiles) {
                                            selectedTab = DetailsTab.SeasonsFiles
                                        } else if (prev && !state.hasSeasonsOrFiles) {
                                            if (selectedTab == DetailsTab.SeasonsFiles) {
                                                selectedTab = DetailsTab.Overview
                                            }
                                        }
                                        previousHasSeasonsOrFiles = state.hasSeasonsOrFiles
                                    }

                                    val availableTabs = state.getAvailableTabs(tracearrState.isTracearrConfigured)

                                    LaunchedEffect(availableTabs) {
                                        if (selectedTab !in availableTabs) {
                                            selectedTab = state.defaultTab
                                        }
                                    }

                                    if (availableTabs.size > 1) {
                                        PrimaryScrollableTabRow(
                                            selectedTabIndex = availableTabs.indexOf(selectedTab).coerceAtLeast(0),
                                            modifier = Modifier.fillMaxWidth(),
                                            edgePadding = 0.dp,
                                        ) {
                                            availableTabs.forEach { tab ->
                                                Tab(
                                                    selected = selectedTab == tab,
                                                    onClick = { selectedTab = tab },
                                                    text = {
                                                        Text(
                                                            when (tab) {
                                                                DetailsTab.SeasonsFiles ->
                                                                    if (state.seasons.isNotEmpty()) {
                                                                        mokoString(MR.strings.seasons_header)
                                                                    } else {
                                                                        mokoString(MR.strings.media)
                                                                    }
                                                                DetailsTab.Overview -> mokoString(MR.strings.overview)
                                                                DetailsTab.Analytics -> mokoString(MR.strings.statistics)
                                                                DetailsTab.History -> mokoString(MR.strings.history)
                                                            },
                                                        )
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }

                                when (selectedTab) {
                                    DetailsTab.SeasonsFiles -> {
                                        SeasonsFilesTabContent(
                                            state = state,
                                            automaticSearchIds = automaticSearchIds,
                                            deleteSeasonStatus = deleteSeasonStatus,
                                            deleteAlbumStatus = deleteAlbumStatus,
                                            onQueueItemClicked = { item -> selectedQueueItem = item },
                                            onToggleSeasonMonitor = { viewModel.toggleSeasonMonitored(it) },
                                            onToggleEpisodeMonitor = { viewModel.toggleEpisodeMonitored(it) },
                                            onEpisodeAutomaticSearch = { viewModel.performEpisodeAutomaticLookup(it) },
                                            onSeasonAutomaticSearch = { viewModel.performSeasonAutomaticLookup(it) },
                                            onDeleteSeasonFiles = { confirmDeleteSeasonNumber = it },
                                            onNavigateToEpisodeDetails = onNavigateToEpisodeDetails,
                                            onDeleteEpisodeFile = { confirmDeleteEpisodeId = it },
                                            onNavigateToSeriesRelease = onNavigateToSeriesRelease,
                                            onPerformAutomaticLookup = { viewModel.performAutomaticLookup() },
                                            onDeleteMovieFile = { confirmDeleteMovie = true },
                                            onNavigateToMovieFiles = onNavigateToMovieFiles,
                                            onNavigateToMovieReleases = onNavigateToMovieReleases,
                                            onToggleAlbumMonitor = { viewModel.toggleAlbumMonitored(it) },
                                            onEditAlbum = { editAlbum = it },
                                            onAlbumAutomaticSearch = { viewModel.performAlbumAutomaticLookup(it) },
                                            onDeleteAlbumFiles = { confirmDeleteAlbum = it },
                                            onNavigateToAlbumRelease = onNavigateToAlbumRelease,
                                            onToggleBookMonitor = { viewModel.toggleBookMonitored(it) },
                                            onToggleBookSeriesMonitor = { viewModel.toggleBookSeriesMonitored(it) },
                                            onBookAutomaticSearch = { viewModel.performBookAutomaticLookup(it) },
                                            onNavigateToAuthorFiles = onNavigateToAuthorFiles,
                                            onNavigateToBookDetails = onNavigateToBookDetails,
                                            onNavigateToBookRelease = onNavigateToBookRelease,
                                            onNavigateToAudiobookFiles = onNavigateToAudiobookFiles,
                                            onNavigateToAudiobookRelease = onNavigateToAudiobookRelease,
                                        )
                                    }

                                    DetailsTab.Overview -> {
                                        OverviewTabContent(
                                            state = state,
                                            qualityProfiles = qualityProfiles,
                                            tags = tags,
                                            activeInstance = activeInstance,
                                            activeSeerrInstance = activeSeerrInstance,
                                            isExpanded = isExpanded,
                                            isDualPanel = isDualPanel,
                                            onEditPath = { showEditPathSheet = true },
                                            onPersonClick = onPersonClick,
                                        )
                                    }

                                    DetailsTab.Analytics -> {
                                        TracearrAnalyticsSection(
                                            uiState = tracearrState,
                                            onWindowSelected = { viewModel.selectTracearrStatsWindow(it) },
                                            modifier = Modifier.padding(horizontal = 24.dp),
                                        )
                                    }

                                    DetailsTab.History -> {
                                        TracearrHistorySection(
                                            uiState = tracearrState,
                                            onLoadMore = { viewModel.loadMoreTracearrHistory() },
                                            onClickItem = { selectedTracearrStreamSession = it.toStreamSession() },
                                            isLargeScreen = isExpanded,
                                            modifier = Modifier.padding(horizontal = 24.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                uiState is UnifiedMediaDetailsUiState.Error -> {
                    Text(
                        text = (uiState as UnifiedMediaDetailsUiState.Error).message ?: "",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    )
                }
                else -> {
                    LoadingIndicator(
                        modifier =
                            Modifier
                                .size(96.dp)
                                .align(Alignment.Center),
                    )
                }
            }
            lastSuccessState?.let { state ->
                val isRequest4k by viewModel.isRequest4k.collectAsStateWithLifecycle()
                SeerrRequestSheetHost(
                    visible = isRequestSheetVisible,
                    details = state.seerrMedia,
                    serviceDetails = serviceDetails,
                    currentUser = currentUser,
                    users = users,
                    requestInProgress = requestStatus is OperationStatus.InProgress,
                    onSubmitRequest = { profileId, rootFolder, langId, seasons, userId ->
                        viewModel.submitRequest(
                            profileId,
                            rootFolder,
                            langId,
                            seasons,
                            is4k = isRequest4k,
                            userId = userId,
                        )
                    },
                    onDismiss = { viewModel.hideRequestSheet() },
                )

                SeerrReportIssueSheetHost(
                    visible = isReportIssueSheetVisible,
                    state = reportIssueState,
                    onUpdateIssueType = { viewModel.setIssueType(it) },
                    onUpdateMessage = { viewModel.setIssueMessage(it) },
                    onUpdateProblemSeason = { viewModel.setProblemSeason(it) },
                    onUpdateProblemEpisode = { viewModel.setProblemEpisode(it) },
                    onReset = { viewModel.resetIssueState() },
                    onSubmit = { viewModel.submitIssue() },
                    onDismiss = { viewModel.hideReportIssueSheet() },
                )
                AddMediaSheetsHost(
                    visible = showAddSheet,
                    state = state,
                    addSheetUiState = addSheetUiState,
                    qualityProfiles = qualityProfiles,
                    rootFolders = rootFolders,
                    tags = tags,
                    addItemStatus = addItemStatus,
                    preferences = preferences,
                    onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                    onSmartAdd = { newItem, searchOnAdd, targetInstId ->
                        viewModel.smartAdd(newItem, searchOnAdd, targetInstId)
                    },
                    onUpdatePreferences = viewModel::updatePreferences,
                    onDismiss = { showAddSheet = false },
                )

                SeerrViewRequestSheetHost(
                    visible = isViewRequestSheetVisible,
                    details = state.seerrMedia,
                    serviceDetails = serviceDetails,
                    requestInProgress = requestStatus is OperationStatus.InProgress,
                    onApproveRequest = { requestId, profileId, rootFolder, languageProfileId, seasons ->
                        viewModel.approveRequest(
                            requestId = requestId,
                            profileId = profileId,
                            rootFolder = rootFolder,
                            languageProfileId = languageProfileId,
                            seasons = seasons,
                        )
                    },
                    onDeclineRequest = { requestId ->
                        viewModel.declineRequest(requestId)
                    },
                    onDismiss = { viewModel.hideViewRequestSheet() },
                )

                EditMediaSheetsHost(
                    showEditPathSheet = showEditPathSheet,
                    showEditSheet = showEditSheet,
                    editAlbum = editAlbum,
                    arrMedia = state.arrMedia,
                    qualityProfiles = qualityProfiles,
                    rootFolders = rootFolders,
                    tags = tags,
                    editStatus = editStatus,
                    onEditItem = { updatedItem, moveFiles ->
                        viewModel.editItem(updatedItem, moveFiles = moveFiles)
                    },
                    onEditMedia = { viewModel.editItem(it) },
                    onUpdateAlbum = { viewModel.updateAlbum(it) },
                    onRequestMoveFiles = { moveFilesItem = it },
                    onDismissEditPath = { showEditPathSheet = false },
                    onDismissEditMedia = { showEditSheet = false },
                    onDismissEditAlbum = { editAlbum = null },
                )

                moveFilesItem?.let { item ->
                    ConfirmMoveFilesDialog(
                        rootFolderPath = item.rootFolderPath,
                        onConfirmMove = {
                            viewModel.editItem(item, moveFiles = true)
                            moveFilesItem = null
                        },
                        onConfirmKeep = {
                            viewModel.editItem(item)
                            moveFilesItem = null
                        },
                        onDismiss = { moveFilesItem = null },
                    )
                }

                if (confirmDelete) {
                    ConfirmDeleteAlert(
                        deleteInProgress = deleteStatus is OperationStatus.InProgress,
                        onDismiss = { confirmDelete = false },
                        onDelete = { deleteFiles, addExclusion ->
                            viewModel.deleteMedia(deleteFiles, addExclusion)
                        },
                    )
                }

                confirmDeleteSeasonNumber?.let { seasonNumber ->
                    ConfirmDeleteSeasonDialog(
                        seasonNumber = seasonNumber,
                        onConfirm = {
                            viewModel.deleteSeasonFiles(seasonNumber)
                            confirmDeleteSeasonNumber = null
                        },
                        onDismiss = { confirmDeleteSeasonNumber = null },
                    )
                }

                confirmDeleteAlbum?.let { albumId ->
                    ConfirmDeleteAlbumDialog(
                        onConfirm = {
                            viewModel.deleteAlbumFiles(albumId)
                            confirmDeleteAlbum = null
                        },
                        onDismiss = { confirmDeleteAlbum = null },
                    )
                }

                confirmDeleteEpisodeId?.let { episodeFileId ->
                    ConfirmDeleteEpisodeDialog(
                        onConfirm = {
                            viewModel.deleteEpisodeFile(episodeFileId)
                            confirmDeleteEpisodeId = null
                        },
                        onDismiss = { confirmDeleteEpisodeId = null },
                    )
                }

                if (confirmDeleteMovie) {
                    ConfirmDeleteMovieDialog(
                        onConfirm = {
                            confirmDeleteMovie = false
                            viewModel.deleteMovieFile()
                        },
                        onDismiss = { confirmDeleteMovie = false },
                    )
                }

                if (confirmDeleteAudiobookFile) {
                    ConfirmDeleteAudiobookDialog(
                        onConfirm = {
                            confirmDeleteAudiobookFile = false
                            viewModel.deleteAudiobookFile()
                        },
                        onDismiss = { confirmDeleteAudiobookFile = false },
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
                        },
                    )
                }

                if (confirmRemoveFromService) {
                    val serviceName =
                        buttonState.serviceName ?: if (requestType == RequestType.Movie) "Radarr" else "Sonarr"
                    ConfirmDeleteSeerrFileDialog(
                        serviceName = serviceName,
                        onConfirm = {
                            viewModel.deleteSeerrMediaFile(is4k = false)
                            confirmRemoveFromService = false
                        },
                        onDismiss = { confirmRemoveFromService = false },
                    )
                }

                if (confirmClearData) {
                    ConfirmClearSeerrDataDialog(
                        onConfirm = {
                            viewModel.clearSeerrMediaData()
                            confirmClearData = false
                        },
                        onDismiss = { confirmClearData = false },
                    )
                }

                pendingSeerrRequest?.let { request ->
                    PendingSeerrRequestDialog(
                        onAction = { action, rememberChoice ->
                            viewModel.handlePendingRequestAction(request.id, action, rememberChoice)
                        },
                        onDismiss = { viewModel.dismissPendingRequestDialog() },
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
    }
}
