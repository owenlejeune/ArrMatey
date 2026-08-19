package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.entensions.unlessEmpty
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AlbumsArea
import com.dnfapps.arrmatey.ui.components.AudiobookFileView
import com.dnfapps.arrmatey.ui.components.BooksArea
import com.dnfapps.arrmatey.ui.components.ConfirmDeleteAlert
import com.dnfapps.arrmatey.ui.components.InfoArea
import com.dnfapps.arrmatey.ui.components.InstanceChipsRow
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MediaActivitySection
import com.dnfapps.arrmatey.ui.components.MovieFileView
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.SeasonsArea
import com.dnfapps.arrmatey.ui.components.SeerrCreditsSection
import com.dnfapps.arrmatey.ui.components.ToolbarAddButton
import com.dnfapps.arrmatey.ui.components.UnifiedDetailsHeader
import com.dnfapps.arrmatey.ui.components.bazarr.BazarrSubtitlesSection
import com.dnfapps.arrmatey.ui.components.buildUnifiedInfoItems
import com.dnfapps.arrmatey.ui.components.buttons.MediaDetailsActions
import com.dnfapps.arrmatey.ui.sheets.AddMovieSheet
import com.dnfapps.arrmatey.ui.sheets.AddSeriesSheet
import com.dnfapps.arrmatey.ui.sheets.EditAlbumSheet
import com.dnfapps.arrmatey.ui.sheets.EditMediaSheet
import com.dnfapps.arrmatey.ui.sheets.SeerrReportIssueSheet
import com.dnfapps.arrmatey.ui.sheets.SeerrRequestSheet
import com.dnfapps.arrmatey.ui.sheets.SeerrViewRequestSheet
import com.dnfapps.arrmatey.ui.tabs.ConfirmDeleteItemSheet
import com.dnfapps.arrmatey.ui.tabs.QueueItemInfoSheet
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.handleWatchClick
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.viewmodel.UnifiedMediaDetailsViewModel
import com.dnfapps.networking.OperationStatus
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnifiedMediaDetailsScreen(
    arrId: Long? = null,
    tmdbId: Long? = null,
    tvdbId: Long? = null,
    instanceType: InstanceType? = null,
    requestType: RequestType? = null,
    isExpanded: Boolean = false,
    viewModel: UnifiedMediaDetailsViewModel = koinInjectParams(arrId, tmdbId, tvdbId, instanceType, requestType),
    moko: MokoStrings = koinInject(),
    onBack: () -> Unit,
    onNavigateToEpisodeDetails: (ArrSeries, Episode) -> Unit = { _, _ -> },
    onNavigateToSeriesRelease: (Long?, Int) -> Unit = { _, _ -> },
    onNavigateToMovieFiles: (ArrMovie) -> Unit = {},
    onNavigateToMovieReleases: (Long) -> Unit = {},
    onNavigateToAuthorFiles: (Author) -> Unit = {},
    onNavigateToBookDetails: (Author, Book) -> Unit = { _, _ -> },
    onNavigateToBookRelease: (Long) -> Unit = {},
    onNavigateToAudiobookFiles: (Audiobook) -> Unit = {},
    onNavigateToAudiobookRelease: (Long?, String?) -> Unit = { _, _ -> },
    onNavigateToAlbumRelease: (Long, Long) -> Unit = { _, _ -> },
    onPersonClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var confirmDelete by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var moveFilesItem by remember { mutableStateOf<ArrMedia?>(null) }
    var confirmDeleteSeasonNumber by remember { mutableStateOf<Int?>(null) }
    var confirmDeleteAlbum by remember { mutableStateOf<Long?>(null) }
    var confirmDeleteMovie by remember { mutableStateOf(false) }
    var editAlbum by remember { mutableStateOf<ArrAlbum?>(null) }
    var selectedQueueItem by remember { mutableStateOf<QueueItem?>(null) }
    var showConfirmRemoveQueueItem by remember { mutableStateOf(false) }

    val qualityProfiles by viewModel.qualityProfiles.collectAsStateWithLifecycle()
    val rootFolders by viewModel.rootFolders.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val editStatus by viewModel.editStatus.collectAsStateWithLifecycle()
    val deleteStatus by viewModel.deleteStatus.collectAsStateWithLifecycle()
    val deleteSeasonStatus by viewModel.deleteSeasonStatus.collectAsStateWithLifecycle()
    val deleteAlbumStatus by viewModel.deleteAlbumStatus.collectAsStateWithLifecycle()
    val deleteMovieFileStatus by viewModel.deleteMovieFileStatus.collectAsStateWithLifecycle()
    val removeQueueItemStatus by viewModel.removeQueueItemStatus.collectAsStateWithLifecycle()

    val isRequestSheetVisible by viewModel.isRequestSheetVisible.collectAsStateWithLifecycle()
    val isReportIssueSheetVisible by viewModel.isReportIssueSheetVisible.collectAsStateWithLifecycle()
    val isViewRequestSheetVisible by viewModel.isViewRequestSheetVisible.collectAsStateWithLifecycle()
    val reportIssueState by viewModel.reportIssueState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val serviceDetails by viewModel.serviceDetails.collectAsStateWithLifecycle()
    val isArrConfigured by viewModel.isArrConfigured.collectAsStateWithLifecycle()
    val isSeerrConfigured by viewModel.isSeerrConfigured.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    val automaticSearchIds by viewModel.automaticSearchIds.collectAsStateWithLifecycle()
    val lastSearchResult by viewModel.lastSearchResult.collectAsStateWithLifecycle()
    val addSheetUiState by viewModel.addSheetUiState.collectAsStateWithLifecycle()
    val searchQueuedMessage = mokoString(MR.strings.search_queued)
    val searchErrorMessage = mokoString(MR.strings.search_error)
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

    Scaffold(
        topBar = {
            OverlayTopAppBar(
                scrollState = scrollState,
                navigationIcon = {
                    IconButton(
                        onClick = { onBack() },
                        colors = IconButtonDefaults.headerBarColors()
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Close else Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = mokoString(if (isExpanded) MR.strings.close else MR.strings.back)
                        )
                    }
                },
                actions = {
                    (uiState as? UnifiedMediaDetailsUiState.Success)?.let { success ->
                        val isMonitored by viewModel.isMonitored.collectAsStateWithLifecycle()
                        val buttonState by viewModel.buttonState.collectAsStateWithLifecycle()

                        AnimatedVisibility(
                            visible = buttonState.showReportIssueButton,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            IconButton(
                                onClick = { viewModel.showReportIssueSheet() },
                                colors = IconButtonDefaults.headerBarColors()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = mokoString(MR.strings.report_issue),
                                    tint = ArrOrange
                                )
                            }
                        }

                        AnimatedContent(
                            targetState = success.hasArrId,
                            transitionSpec = {
                                (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                            },
                            label = "ToolbarActionsAnimation"
                        ) { hasArrId ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (hasArrId) {
                                    if (isArrConfigured) {
                                        IconButton(
                                            onClick = { viewModel.toggleMonitored() },
                                            colors = IconButtonDefaults.headerBarColors()
                                        ) {
                                            AnimatedContent(
                                                targetState = isMonitored,
                                                transitionSpec = {
                                                    (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
                                                },
                                                label = "BookmarkIconAnimation"
                                            ) { monitored ->
                                                Icon(
                                                    imageVector = if (monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                        MenuButton(
                                            onRefresh = { viewModel.refresh() },
                                            onEdit = { showEditSheet = true },
                                            onDelete = { confirmDelete = true },
                                            showSearch = instanceType?.includeTopLevelAutomaticSearchOption == true,
                                            enableSearch = isMonitored,
                                            onSearchMonitored = { viewModel.performAutomaticLookup() },
                                            extraMenuItems = { closeMenu ->
                                                for (missingInstance in success.missingInstances) {
                                                    DropdownMenuItem(
                                                        text = { Text(mokoString(MR.strings.add_to_arr, missingInstance.label)) },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector = Icons.Default.Add,
                                                                contentDescription = null
                                                            )
                                                        },
                                                        onClick = {
                                                            closeMenu()
                                                            viewModel.setAddSheetTargetInstance(missingInstance)
                                                            showAddSheet = true
                                                        }
                                                    )
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    val canAddDirectly = success.arrMedia != null && isArrConfigured
                                    ToolbarAddButton(
                                        canAddDirectly = canAddDirectly,
                                        isSeerrConfigured = isSeerrConfigured,
                                        pendingRequestId = buttonState.pendingRequestId,
                                        resolvedInstanceType = viewModel.resolvedInstanceType,
                                        onAddDirectlyClicked = { showAddSheet = true },
                                        onViewRequestClicked = { viewModel.showViewRequestSheet() },
                                        onRequestClicked = { viewModel.showRequestSheet() }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues.copy(bottom = 0.dp, top = 0.dp))
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is UnifiedMediaDetailsUiState.Initial,
                is UnifiedMediaDetailsUiState.Loading -> {
                    LoadingIndicator(
                        modifier = Modifier
                            .size(96.dp)
                            .align(Alignment.Center)
                    )
                }

                is UnifiedMediaDetailsUiState.Error -> {
                    Text(text = state.message ?: "")
                }

                is UnifiedMediaDetailsUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = { viewModel.refresh() }
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                genres = state.genres
                            )

                            Column(
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                ) {
                                    val title = state.displayTitle ?: mokoString(MR.strings.unknown)
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.headlineMedium
                                    )

                                    state.tagline?.unlessEmpty {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }

                                    state.upcomingDateString?.let { airingString ->
                                        Text(
                                            text = airingString,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                val buttonState by viewModel.buttonState.collectAsStateWithLifecycle()
                                MediaDetailsActions(
                                    buttonState = buttonState,
                                    onWatchClicked = { url, provider ->
                                        handleWatchClick(url, provider, context, moko)
                                    },
                                    onWatchTrailerClicked = { trailerUrl ->
                                        context.openLink(
                                            trailerUrl
                                        )
                                    },
                                    onViewRequestClicked = { requestId -> viewModel.showViewRequestSheet() },
                                    onApproveRequestClicked = { requestId -> viewModel.showViewRequestSheet() },
                                    onDeclineRequestClicked = { requestId ->
                                        viewModel.declineRequest(
                                            requestId
                                        )
                                    },
                                    onRequestClicked = { viewModel.showRequestSheet() },
                                    onRequest4kClicked = { },
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )

                                state.overview?.unlessEmpty {
                                    ItemDescriptionCard(
                                        overview = it,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }

                                if (state.instancePresences.size > 1) {
                                    InstanceChipsRow(
                                        presences = state.instancePresences,
                                        selectedInstanceId = state.selectedInstanceId,
                                        onInstanceSelected = { instanceId ->
                                            viewModel.selectInstance(instanceId)
                                        },
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }

                                if (state.queueItems.isNotEmpty()) {
                                    MediaActivitySection(
                                        queueItems = state.queueItems,
                                        onQueueItemClicked = { item ->
                                            selectedQueueItem = item
                                        },
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }

                                if (state.seasons.isNotEmpty()) {
                                    val arrSeries = state.arrMedia as? ArrSeries
                                    SeasonsArea(
                                        seasons = state.seasons,
                                        seriesId = arrSeries?.id,
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                        searchIds = automaticSearchIds,
                                        onToggleSeasonMonitor = { viewModel.toggleSeasonMonitored(it) },
                                        onToggleEpisodeMonitor = { viewModel.toggleEpisodeMonitored(it) },
                                        onEpisodeAutomaticSearch = { viewModel.performEpisodeAutomaticLookup(it) },
                                        onSeasonAutomaticSearch = { viewModel.performSeasonAutomaticLookup(it) },
                                        deleteSeasonFiles = { confirmDeleteSeasonNumber = it },
                                        seasonDeleteInProgress = deleteSeasonStatus is OperationStatus.InProgress,
                                        onNavigateToEpisodeDetails = { episode ->
                                            arrSeries?.let { series -> onNavigateToEpisodeDetails(series, episode) }
                                        },
                                        onNavigateToSeriesRelease = onNavigateToSeriesRelease
                                    )
                                }

                                AnimatedVisibility(
                                    visible = state.hasArrId,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    when (val item = state.arrMedia) {
                                        is ArrMovie -> {
                                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                MovieFileView(
                                                    modifier = Modifier.padding(horizontal = 24.dp),
                                                    movie = item,
                                                    movieExtraFiles = state.extraFiles,
                                                    searchIds = automaticSearchIds,
                                                    onAutomaticSearch = { viewModel.performAutomaticLookup() },
                                                    onDeleteFile = { confirmDeleteMovie = true },
                                                    onNavigateToMovieFiles = onNavigateToMovieFiles,
                                                    onNavigateToMovieReleases = onNavigateToMovieReleases
                                                )
                                                item.id?.let { movieId ->
                                                    BazarrSubtitlesSection(
                                                        target = BazarrMediaTarget.Movie(movieId),
                                                        modifier = Modifier.padding(horizontal = 24.dp)
                                                    )
                                                }
                                            }
                                        }

                                        is Arrtist -> AlbumsArea(
                                            modifier = Modifier.padding(horizontal = 24.dp),
                                            artist = item,
                                            albums = state.albums,
                                            tracks = state.tracks,
                                            trackFiles = state.trackFiles,
                                            searchIds = automaticSearchIds,
                                            onToggleAlbumMonitor = { viewModel.toggleAlbumMonitored(it) },
                                            onEditAlbum = { editAlbum = it },
                                            onAlbumAutomaticSearch = { viewModel.performAlbumAutomaticLookup(it) },
                                            deleteAlbumFiles = { confirmDeleteAlbum = it },
                                            albumDeleteInProgress = deleteAlbumStatus is OperationStatus.InProgress,
                                            onNavigateToAlbumRelease = onNavigateToAlbumRelease
                                        )

                                        is Author -> BooksArea(
                                            modifier = Modifier.padding(horizontal = 24.dp),
                                            author = item,
                                            series = state.bookSeries,
                                            files = state.bookFiles,
                                            books = state.books,
                                            searchIds = automaticSearchIds,
                                            onToggleMonitor = { viewModel.toggleBookMonitored(it) },
                                            onToggleSeriesMonitor = { viewModel.toggleBookSeriesMonitored(it) },
                                            onAutomaticSearch = { viewModel.performBookAutomaticLookup(it) },
                                            onNavigateToAuthorFiles = onNavigateToAuthorFiles,
                                            onNavigateToBookDetails = onNavigateToBookDetails,
                                            onNavigateToBookRelease = onNavigateToBookRelease
                                        )

                                        is Audiobook -> AudiobookFileView(
                                            modifier = Modifier.padding(horizontal = 24.dp),
                                            audiobook = item,
                                            searchIds = automaticSearchIds,
                                            onAutomaticSearch = { item.id?.let { viewModel.performBookAutomaticLookup(it) } },
                                            onNavigateToAudiobookFiles = onNavigateToAudiobookFiles,
                                            onNavigateToAudiobookRelease = onNavigateToAudiobookRelease
                                        )

                                        is ArrSeries, is SearchAudiobook, is MockMedia, null -> {}
                                    }
                                }

                                state.seerrMedia?.credits?.let { credits ->
                                    SeerrCreditsSection(credits) { onPersonClick(it) }
                                }

                                val infoItems = buildUnifiedInfoItems(state, qualityProfiles, tags)
                                if (infoItems.isNotEmpty()) {
                                    InfoArea(
                                        infoItems,
                                        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    if (isRequestSheetVisible) {
                        state.seerrMedia?.let { seerrMedia ->
                            SeerrRequestSheet(
                                details = seerrMedia,
                                serviceDetails = serviceDetails,
                                currentUser = currentUser,
                                users = users,
                                onDismissRequest = { viewModel.hideRequestSheet() },
                                onSubmitRequest = { profileId, rootFolder, langId, seasons, userId ->
                                    viewModel.submitRequest(profileId, rootFolder, langId, seasons, userId = userId)
                                }
                            )
                        }
                    }
                    if (isReportIssueSheetVisible) {
                        SeerrReportIssueSheet(
                            state = reportIssueState,
                            updateIssueType = { viewModel.setIssueType(it) },
                            updateMessage = { viewModel.setIssueMessage(it) },
                            updateProblemSeason = { viewModel.setProblemSeason(it) },
                            updateProblemEpisode = { viewModel.setProblemEpisode(it) },
                            onReset = { viewModel.resetIssueState() },
                            onSubmit = { viewModel.submitIssue() },
                            onDismiss = { viewModel.hideReportIssueSheet() }
                        )
                    }
                    if (showAddSheet) {
                        state.arrMedia?.let { arrMedia ->
                            when (arrMedia) {
                                is ArrSeries -> AddSeriesSheet(
                                    item = arrMedia,
                                    qualityProfiles = addSheetUiState.qualityProfiles.ifEmpty { qualityProfiles },
                                    rootFolders = addSheetUiState.rootFolders.ifEmpty { rootFolders },
                                    tags = addSheetUiState.tags.ifEmpty { tags },
                                    addInProgress = editStatus is OperationStatus.InProgress,
                                    preferences = preferences,
                                    instances = addSheetUiState.availableInstances,
                                    selectedInstance = addSheetUiState.targetInstance ?: addSheetUiState.availableInstances.firstOrNull(),
                                    onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                    onAddItem = { newItem, searchOnAdd ->
                                        viewModel.smartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                                        showAddSheet = false
                                    },
                                    onUpdatePreferences = viewModel::updatePreferences,
                                    onDismiss = { showAddSheet = false }
                                )

                                is ArrMovie -> AddMovieSheet(
                                    item = arrMedia,
                                    qualityProfiles = addSheetUiState.qualityProfiles.ifEmpty { qualityProfiles },
                                    rootFolders = addSheetUiState.rootFolders.ifEmpty { rootFolders },
                                    tags = addSheetUiState.tags.ifEmpty { tags },
                                    addInProgress = editStatus is OperationStatus.InProgress,
                                    preferences = preferences,
                                    instances = addSheetUiState.availableInstances,
                                    selectedInstance = addSheetUiState.targetInstance ?: addSheetUiState.availableInstances.firstOrNull(),
                                    onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                    onAddItem = { newItem, searchOnAdd ->
                                        viewModel.smartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                                        showAddSheet = false
                                    },
                                    onUpdatePreferences = viewModel::updatePreferences,
                                    onDismiss = { showAddSheet = false }
                                )

                                else -> {}
                            }
                        }
                    }
                    if (isViewRequestSheetVisible) {
                        state.seerrMedia?.let { seerrMedia ->
                            SeerrViewRequestSheet(
                                details = seerrMedia,
                                serviceDetails = serviceDetails,
                                onDismissRequest = { viewModel.hideViewRequestSheet() },
                                onApproveRequest = { requestId, profileId, rootFolder, languageProfileId, seasons ->
                                    viewModel.approveRequest(
                                        requestId = requestId,
                                        profileId = profileId,
                                        rootFolder = rootFolder,
                                        languageProfileId = languageProfileId,
                                        seasons = seasons
                                    )
                                    viewModel.hideViewRequestSheet()
                                },
                                onDeclineRequest = { requestId ->
                                    viewModel.declineRequest(requestId)
                                    viewModel.hideViewRequestSheet()
                                }
                            )
                        }
                    }

                    if (showEditSheet) {
                        state.arrMedia?.let { arrMedia ->
                            EditMediaSheet(
                                item = arrMedia,
                                qualityProfiles = qualityProfiles,
                                rootFolders = rootFolders,
                                tags = tags,
                                editInProgress = editStatus is OperationStatus.InProgress,
                                onEditItem = { viewModel.editItem(it) },
                                onDismiss = { showEditSheet = false }
                            )
                        }
                    }

                    if (confirmDelete) {
                        ConfirmDeleteAlert(
                            deleteInProgress = deleteStatus is OperationStatus.InProgress,
                            onDismiss = { confirmDelete = false },
                            onDelete = { deleteFiles, addExclusion ->
                                viewModel.deleteMedia(deleteFiles, addExclusion)
                            }
                        )
                    }

                    confirmDeleteSeasonNumber?.let { seasonNumber ->
                        AlertDialog(
                            onDismissRequest = { confirmDeleteSeasonNumber = null },
                            title = {
                                Text(mokoString(MR.strings.delete_season, seasonNumber))
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteSeasonFiles(seasonNumber)
                                        confirmDeleteSeasonNumber = null
                                    }
                                ) {
                                    Text(mokoString(MR.strings.yes))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        confirmDeleteSeasonNumber = null
                                    }
                                ) {
                                    Text(mokoString(MR.strings.no))
                                }
                            }
                        )
                    }

                    confirmDeleteAlbum?.let { albumId ->
                        AlertDialog(
                            onDismissRequest = { confirmDeleteAlbum = null },
                            title = {
                                Text(mokoString(MR.strings.delete_album))
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteAlbumFiles(albumId)
                                        confirmDeleteAlbum = null
                                    }
                                ) {
                                    Text(mokoString(MR.strings.yes))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        confirmDeleteAlbum = null
                                    }
                                ) {
                                    Text(mokoString(MR.strings.no))
                                }
                            }
                        )
                    }

                    editAlbum?.let { album ->
                        EditAlbumSheet(
                            album = album,
                            editInProgress = editStatus is OperationStatus.InProgress,
                            onEditAlbum = {
                                viewModel.updateAlbum(it)
                                editAlbum = null
                            },
                            onDismiss = { editAlbum = null }
                        )
                    }

                    if (confirmDeleteMovie) {
                        AlertDialog(
                            onDismissRequest = { confirmDeleteMovie = false },
                            title = { Text(mokoString(MR.strings.confirm_delete)) },
                            text = { Text(text = mokoString(MR.strings.confirm_delete_file)) },
                            dismissButton = {
                                TextButton(onClick = { confirmDeleteMovie = false }) {
                                    Text(mokoString(MR.strings.cancel))
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    confirmDeleteMovie = false
                                    viewModel.deleteMovieFile()
                                }) {
                                    Text(mokoString(MR.strings.confirm))
                                }
                            }
                        )
                    }
                    selectedQueueItem?.let { item ->
                        QueueItemInfoSheet(
                            item = item,
                            onDismiss = { selectedQueueItem = null },
                            onRemove = { showConfirmRemoveQueueItem = true }
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
                                    skipRedownload = skipRedownload
                                )
                                showConfirmRemoveQueueItem = false
                                selectedQueueItem = null
                            }
                        )
                    }
                }
            }
        }
    }
}
