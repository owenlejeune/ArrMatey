package com.dnfapps.arrmatey.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import androidx.core.net.toUri
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
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.bazarr.state.BazarrMediaTarget
import com.dnfapps.arrmatey.compose.utils.formatWithCommas
import com.dnfapps.arrmatey.entensions.copy
import com.dnfapps.arrmatey.entensions.headerBarColors
import com.dnfapps.arrmatey.entensions.openLink
import com.dnfapps.arrmatey.entensions.unlessEmpty
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.InfoItem
import com.dnfapps.arrmatey.model.InstanceMediaPresence
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.model.toInfoList
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.PersonDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.state.MediaProvider
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AlbumsArea
import com.dnfapps.arrmatey.ui.components.AudiobookFileView
import com.dnfapps.arrmatey.ui.components.BooksArea
import com.dnfapps.arrmatey.ui.components.InfoArea
import com.dnfapps.arrmatey.ui.components.ItemDescriptionCard
import com.dnfapps.arrmatey.ui.components.LabelledSwitch
import com.dnfapps.arrmatey.ui.components.MovieFileView
import com.dnfapps.arrmatey.ui.components.OverlayTopAppBar
import com.dnfapps.arrmatey.ui.components.SeasonsArea
import com.dnfapps.arrmatey.ui.components.SeerrCreditsSection
import com.dnfapps.arrmatey.ui.components.UnifiedDetailsHeader
import com.dnfapps.arrmatey.ui.components.UpcomingDateView
import com.dnfapps.arrmatey.ui.components.bazarr.BazarrSubtitlesSection
import com.dnfapps.arrmatey.ui.components.buttons.MediaDetailsActions
import com.dnfapps.arrmatey.ui.sheets.AddMovieSheet
import com.dnfapps.arrmatey.ui.sheets.AddSeriesSheet
import com.dnfapps.arrmatey.ui.sheets.EditAlbumSheet
import com.dnfapps.arrmatey.ui.sheets.EditArtistSheet
import com.dnfapps.arrmatey.ui.sheets.EditAudiobookSheet
import com.dnfapps.arrmatey.ui.sheets.EditAuthorSheet
import com.dnfapps.arrmatey.ui.sheets.EditMovieSheet
import com.dnfapps.arrmatey.ui.sheets.EditSeriesSheet
import com.dnfapps.arrmatey.ui.sheets.SeerrReportIssueSheet
import com.dnfapps.arrmatey.ui.sheets.SeerrRequestSheet
import com.dnfapps.arrmatey.ui.sheets.SeerrViewRequestSheet
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.format
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

    val qualityProfiles by viewModel.qualityProfiles.collectAsStateWithLifecycle()
    val rootFolders by viewModel.rootFolders.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val editStatus by viewModel.editStatus.collectAsStateWithLifecycle()
    val deleteStatus by viewModel.deleteStatus.collectAsStateWithLifecycle()
    val deleteSeasonStatus by viewModel.deleteSeasonStatus.collectAsStateWithLifecycle()
    val deleteAlbumStatus by viewModel.deleteAlbumStatus.collectAsStateWithLifecycle()
    val deleteMovieFileStatus by viewModel.deleteMovieFileStatus.collectAsStateWithLifecycle()

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
    val addTargetInstance by viewModel.addSheetTargetInstance.collectAsStateWithLifecycle()
    val addQualityProfiles by viewModel.addSheetQualityProfiles.collectAsStateWithLifecycle()
    val addRootFolders by viewModel.addSheetRootFolders.collectAsStateWithLifecycle()
    val addTags by viewModel.addSheetTags.collectAsStateWithLifecycle()
    val availableInstances by viewModel.availableInstances.collectAsStateWithLifecycle()
    val searchQueuedMessage = mokoString(MR.strings.search_queued)
    val searchErrorMessage = mokoString(MR.strings.search_error)

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
                Toast.makeText(context, "Item edited successfully", Toast.LENGTH_SHORT).show()
                showEditSheet = false
                editAlbum = null
            }

            is OperationStatus.Error -> {
                Toast.makeText(context, "Error editing items", Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }
    }

    LaunchedEffect(deleteStatus) {
        when (deleteStatus) {
            is OperationStatus.Success -> {
                Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                onBack()
            }

            is OperationStatus.Error -> {
                Toast.makeText(context, "Error deleting item", Toast.LENGTH_SHORT).show()
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

                        if (buttonState.showReportIssueButton) {
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

                        if (success.hasArrId) {
                            if (isArrConfigured) {
                                IconButton(
                                    onClick = { viewModel.toggleMonitored() },
                                    colors = IconButtonDefaults.headerBarColors()
                                ) {
                                    Icon(
                                        imageVector = if (isMonitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = null
                                    )
                                }
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

                        if (success.hasArrId && isArrConfigured) {
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

                                    state.arrMedia?.let { UpcomingDateView(it) }
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

                                val presentPresences = state.instancePresences.filter { it.isPresent }
                                if (presentPresences.size > 1) {
                                    InstanceChipsRow(
                                        presences = presentPresences,
                                        selectedInstanceId = state.selectedInstanceId,
                                        onInstanceSelected = { instanceId ->
                                            viewModel.selectInstance(instanceId)
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

                                when (val item = state.arrMedia) {
                                    is ArrMovie -> {
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

                                state.seerrMedia?.credits?.let { credits ->
                                    SeerrCreditsSection(credits) { onPersonClick(it) }
                                }

                                val infoItems = infoItems(state, qualityProfiles, tags)
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
                                onSubmitRequest = { profileId, rootFolder, languageProfileId, seasons, userId ->
                                    viewModel.submitRequest(
                                        profileId = profileId,
                                        rootFolder = rootFolder,
                                        languageProfileId = languageProfileId,
                                        seasons = seasons,
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
                                    qualityProfiles = addQualityProfiles.ifEmpty { qualityProfiles },
                                    rootFolders = addRootFolders.ifEmpty { rootFolders },
                                    tags = addTags.ifEmpty { tags },
                                    addInProgress = editStatus is OperationStatus.InProgress,
                                    preferences = preferences,
                                    instances = availableInstances,
                                    selectedInstance = addTargetInstance ?: availableInstances.firstOrNull(),
                                    onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                    onAddItem = { newItem, searchOnAdd ->
                                        viewModel.smartAdd(newItem, searchOnAdd, addTargetInstance?.id)
                                        showAddSheet = false
                                    },
                                    onUpdatePreferences = viewModel::updatePreferences,
                                    onDismiss = { showAddSheet = false }
                                )

                                is ArrMovie -> AddMovieSheet(
                                    item = arrMedia,
                                    qualityProfiles = addQualityProfiles.ifEmpty { qualityProfiles },
                                    rootFolders = addRootFolders.ifEmpty { rootFolders },
                                    tags = addTags.ifEmpty { tags },
                                    addInProgress = editStatus is OperationStatus.InProgress,
                                    preferences = preferences,
                                    instances = availableInstances,
                                    selectedInstance = addTargetInstance ?: availableInstances.firstOrNull(),
                                    onInstanceSelected = { viewModel.setAddSheetTargetInstance(it) },
                                    onAddItem = { newItem, searchOnAdd ->
                                        viewModel.smartAdd(newItem, searchOnAdd, addTargetInstance?.id)
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

                    editAlbum?.let {
                        EditAlbumSheet(
                            album = it,
                            editInProgress = editStatus is OperationStatus.InProgress,
                            onEditAlbum = { album ->
                                viewModel.updateAlbum(album)
                            },
                            onDismiss = { editAlbum = null }
                        )
                    }

                    confirmDeleteSeasonNumber?.let {
                        AlertDialog(
                            onDismissRequest = { confirmDeleteSeasonNumber = null },
                            title = {
                                Text(mokoString(MR.strings.delete_season, it))
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteSeasonFiles(it)
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

                    confirmDeleteAlbum?.let {
                        AlertDialog(
                            onDismissRequest = { confirmDeleteAlbum = null },
                            title = {
                                Text(mokoString(MR.strings.delete_season, it))
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        viewModel.deleteAlbumFiles(it)
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

                    if (confirmDelete) {
                        ConfirmDeleteAlert(
                            deleteInProgress = deleteStatus is OperationStatus.InProgress,
                            onDismiss = { confirmDelete = false },
                            onDelete = { deleteFiles, addExclusion ->
                                viewModel.deleteMedia(deleteFiles, addExclusion)
                            }
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
                }
            }
        }
    }
}

@Composable
private fun infoItems(
    state: UnifiedMediaDetailsUiState.Success,
    qualityProfiles: List<QualityProfile>,
    tags: List<Tag>,

): List<InfoItem> = buildList {
    val arrMedia = state.arrMedia
    if (arrMedia != null && state.hasArrId) {
        val arrMap = when (arrMedia) {
            is ArrSeries -> seriesInfo(arrMedia, qualityProfiles, tags)
            is ArrMovie -> movieInfo(arrMedia, qualityProfiles, tags)
            is Arrtist -> artistInfo(arrMedia, qualityProfiles, tags)
            is Author -> authorInfo(arrMedia, qualityProfiles, tags)
            is Audiobook -> audiobookInfo(arrMedia)
            else -> emptyMap()
        }.toInfoList()
        addAll(arrMap)
    }

    val seerrMedia = state.seerrMedia
    if (seerrMedia != null && seerrMedia !is PersonDetails) {
        val statusLabel = mokoString(MR.strings.status)
        add(InfoItem(statusLabel, seerrMedia.status))

        (seerrMedia as? MovieDetails)?.let { movie ->
            movie.releaseDate?.format("MMM dd, yyyy")?.let { releaseDate ->
                add(InfoItem(mokoString(MR.strings.release_date), releaseDate))
            }
            if (movie.revenue > 0L) {
                add(InfoItem(mokoString(MR.strings.revenue), movie.revenue.formatWithCommas()))
            }
            if (movie.budget > 0L) {
                add(InfoItem(mokoString(MR.strings.budget), movie.budget.formatWithCommas()))
            }
        }

        val countriesText = seerrMedia.productionCountries.joinToString("\n") { it.name }
        if (countriesText.isNotEmpty()) {
            add(InfoItem(mokoString(MR.strings.production_countries), countriesText))
        }
        val studiosText = seerrMedia.productionCompanies.joinToString("\n") { it.name }
        if (studiosText.isNotEmpty()) {
            add(InfoItem(mokoString(MR.strings.studios), studiosText))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmDeleteAlert(
    deleteInProgress: Boolean,
    initialAddExclusion: Boolean = false,
    initialDeleteFiles: Boolean = false,
    onDismiss: () -> Unit,
    onDelete: (Boolean, Boolean) -> Unit
) {
    var addExclusion by remember { mutableStateOf(initialAddExclusion) }
    var deleteFiles by remember { mutableStateOf(initialDeleteFiles) }
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            LabelledSwitch(
                label = mokoString(MR.strings.add_exclusion),
                sublabel = mokoString(MR.strings.add_exclusion_description),
                checked = addExclusion,
                onCheckedChange = { addExclusion = !addExclusion }
            )
            LabelledSwitch(
                label = mokoString(MR.strings.delete_files),
                sublabel = mokoString(MR.strings.delete_files_description),
                checked = deleteFiles,
                onCheckedChange = { deleteFiles = !deleteFiles }
            )
            Button(
                onClick = { onDelete(deleteFiles, addExclusion) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                enabled = !deleteInProgress
            ) {
                if (deleteInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                    Text(text = mokoString(MR.strings.delete))
                }
            }
        }
    }
}

@Composable
private fun EditMediaSheet(
    item: ArrMedia,
    qualityProfiles: List<QualityProfile>,
    rootFolders: List<RootFolder>,
    tags: List<Tag>,
    editInProgress: Boolean,
    onEditItem: (ArrMedia) -> Unit,
    onDismiss: () -> Unit
) {
    when (item) {
        is ArrMovie -> EditMovieSheet(
            item = item,
            qualityProfiles = qualityProfiles,
            rootFolders = rootFolders,
            tags = tags,
            editInProgress = editInProgress,
            onEditItem = onEditItem,
            onDismiss = onDismiss,
        )

        is ArrSeries -> EditSeriesSheet(
            item = item,
            qualityProfiles = qualityProfiles,
            rootFolders = rootFolders,
            tags = tags,
            editInProgress = editInProgress,
            onEditItem = onEditItem,
            onDismiss = onDismiss
        )

        is Arrtist -> EditArtistSheet(
            item = item,
            qualityProfiles = qualityProfiles,
            rootFolders = rootFolders,
            tags = tags,
            editInProgress = editInProgress,
            onEditItem = onEditItem,
            onDismiss = onDismiss
        )

        is Author -> EditAuthorSheet(
            item = item,
            qualityProfiles = qualityProfiles,
            rootFolders = rootFolders,
            tags = tags,
            editInProgress = editInProgress,
            onEditItem = onEditItem,
            onDismiss = onDismiss
        )

        is Audiobook -> EditAudiobookSheet(
            item = item,
            qualityProfiles = qualityProfiles,
            rootFolders = rootFolders,
            editInProgress = editInProgress,
            onEditItem = onEditItem,
            onDismiss = onDismiss
        )

        is SearchAudiobook,
        is MockMedia -> {
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ToolbarAddButton(
    canAddDirectly: Boolean,
    isSeerrConfigured: Boolean,
    pendingRequestId: Long?,
    resolvedInstanceType: InstanceType?,
    onAddDirectlyClicked: () -> Unit,
    onViewRequestClicked: () -> Unit,
    onRequestClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (canAddDirectly && isSeerrConfigured) {
        var showToolbarAddMenu by remember { mutableStateOf(false) }
        Box(modifier = modifier) {
            IconButton(
                onClick = { showToolbarAddMenu = true },
                colors = IconButtonDefaults.headerBarColors()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = mokoString(MR.strings.add)
                )
            }
            DropdownMenuPopup(
                expanded = showToolbarAddMenu,
                onDismissRequest = { showToolbarAddMenu = false }
            ) {
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(0, 1)
                ) {

                    DropdownMenuItem(
                        selected = false,
                        text = { Text(mokoString(MR.strings.add_to_arr, resolvedInstanceType?.name ?: "Arr")) },
                        onClick = {
                            showToolbarAddMenu = false
                            onAddDirectlyClicked()
                        },
                        leadingIcon = { Icon(Icons.Default.Add, null) },
                        shapes = MenuDefaults.itemShape(1, 2)
                    )
                    if (pendingRequestId != null) {
                        DropdownMenuItem(
                            selected = false,
                            text = { Text(mokoString(MR.strings.view_request)) },
                            onClick = {
                                showToolbarAddMenu = false
                                onViewRequestClicked()
                            },
                            leadingIcon = { Icon(Icons.Default.Schedule, null) },
                            shapes = MenuDefaults.itemShape(0, 2)
                        )
                    } else {
                        DropdownMenuItem(
                            selected = false,
                            text = { Text(mokoString(MR.strings.request)) },
                            onClick = {
                                showToolbarAddMenu = false
                                onRequestClicked()
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Default.Send, null) },
                            shapes = MenuDefaults.itemShape(1, 2)
                        )
                    }
                }
            }
        }
    } else if (canAddDirectly) {
        IconButton(
            onClick = onAddDirectlyClicked,
            colors = IconButtonDefaults.headerBarColors(),
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = mokoString(MR.strings.add)
            )
        }
    } else if (isSeerrConfigured) {
        IconButton(
            onClick = {
                if (pendingRequestId != null) {
                    onViewRequestClicked()
                } else {
                    onRequestClicked()
                }
            },
            colors = IconButtonDefaults.headerBarColors(),
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = mokoString(MR.strings.add)
            )
        }
    }
}

fun handleWatchClick(
    url: String,
    provider: MediaProvider,
    context: Context,
    moko: MokoStrings
) {
    when (provider) {
        MediaProvider.Plex -> {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, moko.getString(MR.strings.no_app_found), Toast.LENGTH_SHORT).show()
            }
        }

        MediaProvider.Jellyfin -> {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, moko.getString(MR.strings.no_app_found), Toast.LENGTH_SHORT).show()
            }
        }

        MediaProvider.None -> {
            Toast.makeText(context, moko.getString(MR.strings.no_app_found), Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstanceChipsRow(
    presences: List<InstanceMediaPresence>,
    selectedInstanceId: Long?,
    onInstanceSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        presences.forEach { presence ->
            val instance = presence.instance
            val isSelected = instance.id == selectedInstanceId
            val hasFile = when (val media = presence.arrMedia) {
                is ArrMovie -> media.hasFile
                is ArrSeries -> media.episodeFileCount > 0
                else -> true
            }

            FilterChip(
                selected = isSelected,
                onClick = { onInstanceSelected(instance.id) },
                label = { Text(instance.label) },
                leadingIcon = {
                    Icon(
                        imageVector = if (hasFile) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (hasFile) MaterialTheme.colorScheme.primary else ArrOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}
