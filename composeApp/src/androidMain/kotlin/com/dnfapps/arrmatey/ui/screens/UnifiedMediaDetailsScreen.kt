package com.dnfapps.arrmatey.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
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
import com.dnfapps.arrmatey.arr.api.model.SearchAuthor
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.entensions.unlessEmpty
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.state.MediaButtonState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AlbumsArea
import com.dnfapps.arrmatey.ui.components.AudiobookFileView
import com.dnfapps.arrmatey.ui.components.BooksArea
import com.dnfapps.arrmatey.ui.components.ConfirmDeleteAlert
import com.dnfapps.arrmatey.ui.components.InfoArea
import com.dnfapps.arrmatey.ui.components.InfoCardData
import com.dnfapps.arrmatey.ui.components.InfoCardInstanceFooter
import com.dnfapps.arrmatey.ui.components.InstancePicker
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.MediaActivitySection
import com.dnfapps.arrmatey.ui.components.MovieFileView
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.SeasonsArea
import com.dnfapps.arrmatey.ui.components.SeerrCreditsSection
import com.dnfapps.arrmatey.ui.components.UnifiedDetailsHeader
import com.dnfapps.arrmatey.ui.components.bazarr.BazarrSubtitlesSection
import com.dnfapps.arrmatey.ui.components.buildArrInfoItems
import com.dnfapps.arrmatey.ui.components.buildSeerrInfoItems
import com.dnfapps.arrmatey.ui.components.buttons.MediaDetailsActions
import com.dnfapps.arrmatey.ui.sheets.AddArtistSheet
import com.dnfapps.arrmatey.ui.sheets.AddAudiobookSheet
import com.dnfapps.arrmatey.ui.sheets.AddAuthorSheet
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
    onBack: () -> Unit,
    onNavigateToEpisodeDetails: (ArrSeries, Episode) -> Unit,
    onNavigateToSeriesRelease: (Long?, Int) -> Unit,
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
    viewModel: UnifiedMediaDetailsViewModel = koinInjectParams(arrId, tmdbId, tvdbId, instanceType, requestType, instanceId),
    moko: MokoStrings = koinInject(),
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
    var confirmRemoveFromService by remember { mutableStateOf(false) }
    var confirmClearData by remember { mutableStateOf(false) }

    val qualityProfiles by viewModel.qualityProfiles.collectAsStateWithLifecycle()
    val rootFolders by viewModel.rootFolders.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val addItemStatus by viewModel.addItemStatus.collectAsStateWithLifecycle()
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
                        val showArrActions = success.hasArrId && isArrConfigured
                        val canAddDirectly = !success.hasArrId && success.arrMedia != null && isArrConfigured
                        val resolvedType = viewModel.resolvedInstanceType

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

                        if (resolvedType != null && success.availableInstances.size > 1) {
                            InstancePicker(
                                type = resolvedType,
                                currentInstance = success.availableInstances.firstOrNull { it.id == success.selectedInstanceId },
                                typeInstances = success.availableInstances,
                                onInstanceSelected = { viewModel.selectInstance(it.id) },
                                buttonColors = IconButtonDefaults.headerBarColors()
                            )
                        }

                        if (showArrActions) {
                            IconButton(
                                onClick = { viewModel.toggleMonitored() },
                                colors = IconButtonDefaults.headerBarColors()
                            ) {
                                AnimatedContent(
                                    targetState = isMonitored,
                                    label = "MonitoredIcon"
                                ) { monitored ->
                                    Icon(
                                        imageVector = if (monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = mokoString(if (monitored) MR.strings.unmonitored else MR.strings.monitored)
                                    )
                                }
                            }
                        }

                        if (canAddDirectly) {
                            IconButton(
                                onClick = { showAddSheet = true },
                                colors = IconButtonDefaults.headerBarColors()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = mokoString(MR.strings.add)
                                )
                            }
                        }

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
                            onMarkAsAvailable = { viewModel.markSeerrMediaAsAvailable() },
                            onRemoveFromService = { confirmRemoveFromService = true },
                            onClearData = { confirmClearData = true }
                        )
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

                                    state.upcomingDateString?.unlessEmpty { airingString ->
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
                                    onRequestClicked = { viewModel.showRequestSheet(is4k = false) },
                                    onRequest4kClicked = { viewModel.showRequestSheet(is4k = true) },
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )

                                state.overview?.unlessEmpty {
                                    ItemDescriptionCard(
                                        overview = it,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }

                                AnimatedVisibility(
                                    visible = state.queueItems.isNotEmpty(),
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
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
                                    visible = state.hasArrId && state.arrMedia !is ArrSeries,
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

                                val arrInfoItems = buildArrInfoItems(state, qualityProfiles, tags)
                                val seerrInfoItems = buildSeerrInfoItems(state)
                                val showBothCards = arrInfoItems.isNotEmpty() && seerrInfoItems.isNotEmpty()

                                val selectedArrInstance = state.availableInstances.firstOrNull { it.id == state.selectedInstanceId } ?: activeInstance
                                val selectedSeerrInstance = activeSeerrInstance

                                if (arrInfoItems.isNotEmpty() || seerrInfoItems.isNotEmpty()) {
                                    InfoArea(
                                        cards = listOf(
                                            InfoCardData(
                                                items = arrInfoItems,
                                                footer = if (showBothCards && selectedArrInstance != null) {
                                                    { InfoCardInstanceFooter(selectedArrInstance) }
                                                } else null
                                            ),
                                            InfoCardData(
                                                items = seerrInfoItems,
                                                footer = if (showBothCards && selectedSeerrInstance != null) {
                                                    { InfoCardInstanceFooter(selectedSeerrInstance) }
                                                } else null
                                            )
                                        ),
                                        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()
                                    )
                                }

                                state.keywords.unlessEmpty { keywords ->
                                    val rowCount = minOf(3, maxOf(1, keywords.size))
                                    val rows = (0 until rowCount).map { rowIndex ->
                                        keywords.filterIndexed { index, _ -> index % rowCount == rowIndex }
                                    }

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(0.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 24.dp)
                                    ) {
                                        rows.forEach { rowKeywords ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                rowKeywords.forEach { keyword ->
                                                    SuggestionChip(
                                                        onClick = {},
                                                        label = { Text(keyword.name) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isRequestSheetVisible) {
                        state.seerrMedia?.let { seerrMedia ->
                            val isRequest4k by viewModel.isRequest4k.collectAsStateWithLifecycle()
                            SeerrRequestSheet(
                                details = seerrMedia,
                                serviceDetails = serviceDetails,
                                currentUser = currentUser,
                                users = users,
                                onDismissRequest = { viewModel.hideRequestSheet() },
                                onSubmitRequest = { profileId, rootFolder, langId, seasons, userId ->
                                    viewModel.submitRequest(
                                        profileId,
                                        rootFolder,
                                        langId,
                                        seasons,
                                        is4k = isRequest4k,
                                        userId = userId
                                    )
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
                                    addInProgress = addItemStatus is OperationStatus.InProgress,
                                    preferences = preferences,
                                    instances = addSheetUiState.availableInstances,
                                    selectedInstance = addSheetUiState.targetInstance,
                                    onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                    onAddItem = { newItem, searchOnAdd ->
                                        viewModel.smartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                                    },
                                    onUpdatePreferences = viewModel::updatePreferences,
                                    onDismiss = { showAddSheet = false }
                                )

                                is ArrMovie -> AddMovieSheet(
                                    item = arrMedia,
                                    qualityProfiles = addSheetUiState.qualityProfiles.ifEmpty { qualityProfiles },
                                    rootFolders = addSheetUiState.rootFolders.ifEmpty { rootFolders },
                                    tags = addSheetUiState.tags.ifEmpty { tags },
                                    addInProgress = addItemStatus is OperationStatus.InProgress,
                                    preferences = preferences,
                                    instances = addSheetUiState.availableInstances,
                                    selectedInstance = addSheetUiState.targetInstance,
                                    onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                    onAddItem = { newItem, searchOnAdd ->
                                        viewModel.smartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                                    },
                                    onUpdatePreferences = viewModel::updatePreferences,
                                    onDismiss = { showAddSheet = false }
                                )

                                is Arrtist -> AddArtistSheet(
                                    item = arrMedia,
                                    qualityProfiles = addSheetUiState.qualityProfiles.ifEmpty { qualityProfiles },
                                    rootFolders = addSheetUiState.rootFolders.ifEmpty { rootFolders },
                                    tags = addSheetUiState.tags.ifEmpty { tags },
                                    addInProgress = addItemStatus is OperationStatus.InProgress,
                                    preferences = preferences,
                                    instances = addSheetUiState.availableInstances,
                                    selectedInstance = addSheetUiState.targetInstance,
                                    onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                    onAddItem = { newItem, searchOnAdd ->
                                        viewModel.smartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                                    },
                                    onUpdatePreferences = viewModel::updatePreferences,
                                    onDismiss = { showAddSheet = false }
                                )

                                is Author -> AddAuthorSheet(
                                    item = arrMedia,
                                    qualityProfiles = addSheetUiState.qualityProfiles.ifEmpty { qualityProfiles },
                                    rootFolders = addSheetUiState.rootFolders.ifEmpty { rootFolders },
                                    tags = addSheetUiState.tags.ifEmpty { tags },
                                    addInProgress = addItemStatus is OperationStatus.InProgress,
                                    preferences = preferences,
                                    instances = addSheetUiState.availableInstances,
                                    selectedInstance = addSheetUiState.targetInstance,
                                    onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                    onAddItem = { newItem, searchOnAdd ->
                                        viewModel.smartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                                    },
                                    onUpdatePreferences = viewModel::updatePreferences,
                                    onDismiss = { showAddSheet = false }
                                )

                                is SearchAudiobook -> AddAudiobookSheet(
                                    item = arrMedia,
                                    qualityProfiles = addSheetUiState.qualityProfiles.ifEmpty { qualityProfiles },
                                    rootFolders = addSheetUiState.rootFolders.ifEmpty { rootFolders },
                                    relativePath = "",
                                    addInProgress = addItemStatus is OperationStatus.InProgress,
                                    preferences = preferences,
                                    instances = addSheetUiState.availableInstances,
                                    selectedInstance = addSheetUiState.targetInstance,
                                    onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                    onAddItem = { newItem, searchOnAdd ->
                                        viewModel.smartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                                    },
                                    onUpdatePreferences = viewModel::updatePreferences,
                                    onDismiss = { showAddSheet = false }
                                )

                                is Audiobook -> {
                                    val searchAudiobook = SearchAudiobook(
                                        asin = arrMedia.asin ?: "",
                                        title = arrMedia.title ?: "",
                                        summary = arrMedia.overview,
                                        authors = arrMedia.authors.map { SearchAuthor(name = it) }
                                    )
                                    AddAudiobookSheet(
                                        item = searchAudiobook,
                                        qualityProfiles = addSheetUiState.qualityProfiles.ifEmpty { qualityProfiles },
                                        rootFolders = addSheetUiState.rootFolders.ifEmpty { rootFolders },
                                        relativePath = "",
                                        addInProgress = addItemStatus is OperationStatus.InProgress,
                                        preferences = preferences,
                                        instances = addSheetUiState.availableInstances,
                                        selectedInstance = addSheetUiState.targetInstance,
                                        onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                        onAddItem = { newItem, searchOnAdd ->
                                            viewModel.smartAdd(newItem, searchOnAdd, addSheetUiState.targetInstance?.id)
                                        },
                                        onUpdatePreferences = viewModel::updatePreferences,
                                        onDismiss = { showAddSheet = false }
                                    )
                                }

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
                                onEditItem = {
                                    if (arrMedia.rootFolderPath != it.rootFolderPath) {
                                        moveFilesItem = it
                                    } else {
                                        viewModel.editItem(it)
                                    }
                                },
                                onDismiss = { showEditSheet = false }
                            )
                        }
                    }

                    moveFilesItem?.let { item ->
                        AlertDialog(
                            onDismissRequest = { moveFilesItem = null },
                            title = {
                                Text(mokoString(MR.strings.move_files_confirm, item.rootFolderPath ?: ""))
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.editItem(item, moveFiles = true)
                                        moveFilesItem = null
                                    }
                                ) {
                                    Text(mokoString(MR.strings.yes))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.editItem(item)
                                        moveFilesItem = null
                                    }
                                ) {
                                    Text(mokoString(MR.strings.no))
                                }
                            }
                        )
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
                            }
                        )
                    }

                    if (confirmRemoveFromService) {
                        val serviceName =
                            buttonState.serviceName ?: if (requestType == RequestType.Movie) "Radarr" else "Sonarr"
                        AlertDialog(
                            onDismissRequest = { confirmRemoveFromService = false },
                            title = {
                                Text(mokoString(MR.strings.are_you_sure))
                            },
                            text = {
                                Text(mokoString(MR.strings.remove_from_service_confirm, serviceName))
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteSeerrMediaFile(is4k = false)
                                        confirmRemoveFromService = false
                                    }
                                ) {
                                    Text(
                                        text = mokoString(MR.strings.yes),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { confirmRemoveFromService = false }) {
                                    Text(mokoString(MR.strings.no))
                                }
                            }
                        )
                    }

                    if (confirmClearData) {
                        AlertDialog(
                            onDismissRequest = { confirmClearData = false },
                            title = {
                                Text(mokoString(MR.strings.are_you_sure))
                            },
                            text = {
                                Text(mokoString(MR.strings.clear_data_confirm))
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.clearSeerrMediaData()
                                        confirmClearData = false
                                    }
                                ) {
                                    Text(
                                        text = mokoString(MR.strings.yes),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { confirmClearData = false }) {
                                    Text(mokoString(MR.strings.no))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun UnifiedMediaDetailsToolbarMenu(
    success: UnifiedMediaDetailsUiState.Success,
    buttonState: MediaButtonState,
    instanceType: InstanceType?,
    requestType: RequestType?,
    isArrConfigured: Boolean,
    isSeerrConfigured: Boolean,
    isMonitored: Boolean,
    onRefresh: () -> Unit,
    onAutomaticLookup: () -> Unit,
    onAddMissingInstance: (Instance) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkAsAvailable: () -> Unit,
    onRemoveFromService: () -> Unit,
    onClearData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showArrActions = success.hasArrId && isArrConfigured
    val showSeerrActions =
        isSeerrConfigured && (buttonState.showRemoveFromServiceButton || buttonState.showClearDataButton || buttonState.showMarkAsAvailableButton)
    val showMissingInstances = success.missingInstances.isNotEmpty()
    val showMenuButton = showArrActions || showSeerrActions || showMissingInstances

    if (!showMenuButton) return

    var showMenu by remember { mutableStateOf(false) }
    val menuInteractionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier) {
        IconButton(
            onClick = { showMenu = !showMenu },
            colors = IconButtonDefaults.headerBarColors()
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null
            )
        }

        DropdownMenuPopup(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            val totalGroups = (if (showArrActions) 2 else 0) +
                    (if (showMissingInstances && !showArrActions) 1 else 0) +
                    (if (showSeerrActions) 1 else 0)
            var currentGroup = 0

            if (showArrActions) {
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(currentGroup++, totalGroups),
                    interactionSource = menuInteractionSource
                ) {
                    DropdownMenuItem(
                        text = { Text(mokoString(MR.strings.refresh)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            onRefresh()
                        }
                    )
                    if (instanceType?.includeTopLevelAutomaticSearchOption == true) {
                        DropdownMenuItem(
                            text = { Text(mokoString(MR.strings.search_monitored)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            enabled = isMonitored,
                            onClick = {
                                showMenu = false
                                onAutomaticLookup()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))

                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(currentGroup++, totalGroups),
                    interactionSource = menuInteractionSource
                ) {
                    DropdownMenuItem(
                        text = { Text(mokoString(MR.strings.edit)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = mokoString(MR.strings.delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )

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
                                showMenu = false
                                onAddMissingInstance(missingInstance)
                            }
                        )
                    }
                }
            } else if (showMissingInstances) {
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(currentGroup++, totalGroups),
                    interactionSource = menuInteractionSource
                ) {
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
                                showMenu = false
                                onAddMissingInstance(missingInstance)
                            }
                        )
                    }
                }
            }

            if (showSeerrActions) {
                if (currentGroup > 0) {
                    Spacer(modifier = Modifier.height(MenuDefaults.GroupSpacing))
                }
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(currentGroup++, totalGroups),
                    interactionSource = menuInteractionSource
                ) {
                    if (buttonState.showMarkAsAvailableButton) {
                        val markText = if (requestType == RequestType.Movie) {
                            mokoString(MR.strings.mark_as_available)
                        } else {
                            mokoString(MR.strings.mark_all_seasons_as_available)
                        }
                        DropdownMenuItem(
                            text = { Text(markText) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showMenu = false
                                onMarkAsAvailable()
                            }
                        )
                    }
                    if (buttonState.showRemoveFromServiceButton) {
                        val removeText = if (requestType == RequestType.Movie) {
                            mokoString(MR.strings.remove_from_radarr)
                        } else {
                            mokoString(MR.strings.remove_from_sonarr)
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = removeText,
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onRemoveFromService()
                            }
                        )
                    }
                    if (buttonState.showClearDataButton) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = mokoString(MR.strings.clear_data),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onClearData()
                            }
                        )
                    }
                }
            }
        }
    }
}
