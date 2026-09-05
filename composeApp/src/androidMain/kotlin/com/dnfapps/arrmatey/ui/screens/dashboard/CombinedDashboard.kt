package com.dnfapps.arrmatey.ui.screens.dashboard

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.arr.viewmodel.CombinedDashboardViewModel
import com.dnfapps.arrmatey.compose.DashboardCards
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.discover.viewmodel.DiscoverViewModel
import com.dnfapps.arrmatey.entensions.PaddingValues
import com.dnfapps.arrmatey.entensions.isExpanded
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrAppBarWithSearch
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.screens.requests.IssueDetailsSheet
import com.dnfapps.arrmatey.ui.screens.requests.IssuesList
import com.dnfapps.arrmatey.ui.screens.requests.RequestsList
import com.dnfapps.arrmatey.ui.sheets.HealthNoticesSheet
import com.dnfapps.arrmatey.ui.sheets.SeerrViewRequestSheet
import com.dnfapps.arrmatey.ui.tabs.ConfirmDeleteItemSheet
import com.dnfapps.arrmatey.ui.tabs.DiscoverSearchOverlay
import com.dnfapps.arrmatey.ui.tabs.QueueItemInfoSheet
import com.dnfapps.arrmatey.ui.theme.ArrRed
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState

fun navigateCalendarItem(
    onNavigateToMediaDetails: (id: Long, instanceType: InstanceType) -> Unit,
    item: CalendarItem,
) {
    when (item) {
        is Episode -> onNavigateToMediaDetails(item.seriesId, InstanceType.Sonarr)
        is EpisodeGroup -> onNavigateToMediaDetails(item.first.seriesId, InstanceType.Sonarr)
        is ArrMovie -> item.id?.let { onNavigateToMediaDetails(it, InstanceType.Radarr) }
        is ArrAlbum -> onNavigateToMediaDetails(item.artistId, InstanceType.Lidarr)
        is Book -> item.authorId?.let { onNavigateToMediaDetails(it, InstanceType.Bookshelf) }
        is Audiobook -> item.id?.let { onNavigateToMediaDetails(it, InstanceType.Listenarr) }
    }
}

@Composable
fun DashboardCardContent(
    cardType: DashboardCards,
    currentState: CombinedDashboardState.Success,
    isEditing: Boolean,
    enabled: Boolean = true,
    onNavigateToArrDashboard: (Long) -> Unit = {},
    onNavigateToMediaDetails: (id: Long, instanceType: InstanceType) -> Unit = { _, _ -> },
    onRequestClick: (MediaRequestPackage) -> Unit = {},
    onIssueClick: (MediaIssuePackage) -> Unit = {},
    onRequestActivityItem: (QueueItem) -> Unit = {},
    onHealthClick: () -> Unit = {},
    onSeerrRequestsStatClick: () -> Unit = {},
    onSeerrIssuesStatClick: () -> Unit = {},
) {
    when (cardType) {
        DashboardCards.ArrOverview ->
            DashboardOverviewCards(
                state = currentState,
                isEditing = isEditing,
                onHealthClick = {
                    if (!isEditing && enabled) onHealthClick()
                },
            )

        DashboardCards.SeerrOverview ->
            SeerrSection(
                state = currentState,
                isEditing = isEditing,
                onRequestClick = {
                    if (!isEditing && enabled) onSeerrRequestsStatClick()
                },
                onIssueClick = {
                    if (!isEditing && enabled) onSeerrIssuesStatClick()
                },
            )

        DashboardCards.ProwlarrOverview ->
            DashboardProwlarrSection(
                state = currentState,
                isEditing = isEditing,
            )

        DashboardCards.Network ->
            DashboardNetworkSection(currentState)

        DashboardCards.RecentlyAdded ->
            RecentlyAddedSection(
                enabled = !isEditing && enabled,
                state = currentState,
                onOpenItem = { id, type ->
                    onNavigateToMediaDetails(id, type)
                },
            )

        DashboardCards.DownloadClients ->
            DashboardDownloadClientsSection(
                state = currentState,
                isEditing = isEditing,
            )

        DashboardCards.ActivityQueue ->
            DashboardActivityQueueSection(
                state = currentState,
                isEditing = isEditing,
                enabled = enabled,
                onItemClick = { item ->
                    if (!isEditing && enabled) onRequestActivityItem(item)
                },
            )

        DashboardCards.OnToday ->
            DashboardTodaySection(
                state = currentState,
                isEditing = isEditing,
                enabled = enabled,
                onItemClick = { calendarItem ->
                    if (!isEditing && enabled) navigateCalendarItem(onNavigateToMediaDetails, calendarItem)
                },
            )

        DashboardCards.UpcomingReleases ->
            DashboardUpcomingSection(
                state = currentState,
                isEditing = isEditing,
                enabled = enabled,
                onItemClick = { calendarItem ->
                    if (!isEditing && enabled) navigateCalendarItem(onNavigateToMediaDetails, calendarItem)
                },
            )

        DashboardCards.BazarrOverview ->
            BazarrSection(
                state = currentState,
                isEditing = isEditing,
            )

        DashboardCards.PendingRequests ->
            DashboardPendingRequestsSection(
                state = currentState,
                enabled = !isEditing && enabled,
                onRequestClick = { mediaPackage ->
                    if (!isEditing && enabled) onRequestClick(mediaPackage)
                },
            )

        DashboardCards.PendingIssues ->
            DashboardPendingIssuesSection(
                state = currentState,
                enabled = !isEditing && enabled,
                onIssueClick = { mediaPackage ->
                    if (!isEditing && enabled) onIssueClick(mediaPackage)
                },
            )

        DashboardCards.InstanceDashboard ->
            InstanceDashboardSection(
                state = currentState,
                enabled = !isEditing && enabled,
                onInstanceClicked = { id ->
                    onNavigateToArrDashboard(id)
                },
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CombinedDashboard(
    windowSizeClass: WindowSizeClass,
    viewModel: CombinedDashboardViewModel = koinViewModel(),
    discoverViewModel: DiscoverViewModel = koinViewModel(),
    requestsViewModel: RequestsViewModel = koinViewModel(),
    moko: MokoStrings = koinInject(),
    onNavigateToArrDashboard: (Long) -> Unit = {},
    onNavigateToMediaDetails: (id: Long, instanceType: InstanceType) -> Unit = { _, _ -> },
    onNavigateToSeerrMediaDetails: (tmdbId: Long, requestType: RequestType) -> Unit = { _, _ -> },
    onNavigateToSeerrPersonDetails: (personId: Long) -> Unit = {},
    onNavigateToArrMediaDetailsOrPreview: (media: ArrMedia, instanceType: InstanceType) -> Unit = { _, _ -> },
    onNavigateToSettings: () -> Unit = {},
    onNavigateToRequestsTab: () -> Unit = {},
    onNavigateToProwlarrTab: () -> Unit = {},
    onNavigateToDownloadsTab: () -> Unit = {},
    onNavigateToActivityTab: () -> Unit = {},
    onNavigateToScheduleTab: () -> Unit = {},
    onNavigateToBazarrTab: () -> Unit = {},
) {
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val showFirstLaunchToast by viewModel.showFirstLaunchToast.collectAsStateWithLifecycle()
    val showDashboardSearch by viewModel.showDashboardSearch.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val isEditing by viewModel.isEditing.collectAsStateWithLifecycle()
    val removeItemStatus by viewModel.removeItemState.collectAsStateWithLifecycle()
    val availableCards = remember(cards) { DashboardCards.entries.filter { it !in cards } }

    val searchQuery by discoverViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchState by discoverViewModel.searchState.collectAsStateWithLifecycle()
    val isSearching by discoverViewModel.isSearching.collectAsStateWithLifecycle()
    val searchShowBanners by discoverViewModel.searchShowBanners.collectAsStateWithLifecycle()
    val searchShowInstanceIndicatorShadow by discoverViewModel.searchShowInstanceIndicatorShadow.collectAsStateWithLifecycle()

    val requestsState by requestsViewModel.requestsState.collectAsStateWithLifecycle()
    val issuesState by requestsViewModel.issuesState.collectAsStateWithLifecycle()
    val userState by requestsViewModel.userState.collectAsStateWithLifecycle()
    val operationsState by requestsViewModel.operationsState.collectAsStateWithLifecycle()

    val textFieldState = rememberTextFieldState(searchQuery)
    val searchBarState = rememberSearchBarState()

    LaunchedEffect(textFieldState.text) {
        discoverViewModel.updateSearchQuery(textFieldState.text.toString())
    }

    val gridState = rememberLazyStaggeredGridState()

    var showAddCardSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var selectedRequestForSheet by remember { mutableStateOf<MediaRequestPackage?>(null) }
    var selectedIssueForSheet by remember { mutableStateOf<MediaIssuePackage?>(null) }
    var selectedActivityItem by remember { mutableStateOf<QueueItem?>(null) }
    var showConfirmRemoveActivity by remember { mutableStateOf(false) }
    var showHealthNoticesSheet by remember { mutableStateOf(false) }
    var showSeerrRequestsSheet by remember { mutableStateOf(false) }
    var showSeerrIssuesSheet by remember { mutableStateOf(false) }

    LaunchedEffect(removeItemStatus) {
        if (removeItemStatus is OperationStatus.Success) {
            selectedActivityItem = null
            showConfirmRemoveActivity = false
            viewModel.resetRemoveItemState()
        }
    }

    LaunchedEffect(showFirstLaunchToast) {
        if (showFirstLaunchToast) {
            Toast.makeText(context, moko.getString(MR.strings.dashboard_first_launch), Toast.LENGTH_LONG).show()
            viewModel.setFirstLaunchComplete()
        }
    }

    LaunchedEffect(isEditing) {
        if (isEditing && searchBarState.isExpanded()) {
            searchBarState.animateToCollapsed()
        }
    }

    Scaffold(
        topBar = {
            if (showDashboardSearch) {
                ArrAppBarWithSearch(
                    textFieldEnabled = !isEditing,
                    textFieldState = textFieldState,
                    searchBarState = searchBarState,
                    searchPlaceholder = mokoString(MR.strings.search),
                    navigationIcon = {
                        if (isEditing) {
                            IconButton(onClick = { viewModel.toggleEditing() }) {
                                Icon(Icons.Default.Close, null)
                            }
                        } else if (isCompact) {
                            NavigationDrawerButton()
                        }
                    },
                    actions = {
                        if (isEditing) {
                            IconButton(onClick = { viewModel.toggleDashboardSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            IconButton(onClick = {
                                viewModel.resetCardsOrder()
                                scope.launch {
                                    gridState.animateScrollToItem(0)
                                }
                            }) {
                                Icon(Icons.Default.Restore, null)
                            }
                        }
                    },
                )
            } else {
                TopAppBar(
                    title = { Text(mokoString(MR.strings.dashboard)) },
                    navigationIcon = {
                        if (isEditing) {
                            IconButton(onClick = { viewModel.toggleEditing() }) {
                                Icon(Icons.Default.Close, null)
                            }
                        } else if (isCompact) {
                            NavigationDrawerButton()
                        }
                    },
                    windowInsets = TopAppBarDefaults.windowInsets,
                    actions = {
                        if (isEditing) {
                            IconButton(onClick = { viewModel.toggleDashboardSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = {
                                viewModel.resetCardsOrder()
                                scope.launch {
                                    gridState.animateScrollToItem(0)
                                }
                            }) {
                                Icon(Icons.Default.Restore, null)
                            }
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (isEditing && availableCards.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showAddCardSheet = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(mokoString(MR.strings.add)) },
                )
            }
        },
        contentWindowInsets = WindowInsets(0.dp),
    ) { contentPadding ->
        Box(
            modifier =
                Modifier
                    .padding(top = contentPadding.calculateTopPadding())
                    .fillMaxSize(),
        ) {
            if (showDashboardSearch && searchBarState.isExpanded()) {
                DiscoverSearchOverlay(
                    items = searchState,
                    isLoading = isSearching,
                    showBanners = searchShowBanners,
                    showInstanceIndicatorShadow = searchShowInstanceIndicatorShadow,
                    onItemClick = { result ->
                        when (result) {
                            is SearchResult.ArrMediaResult -> {
                                onNavigateToArrMediaDetailsOrPreview(result.media, result.instanceType)
                            }
                            is SearchResult.SeerrMediaResult -> {
                                onNavigateToSeerrMediaDetails(result.result.id, result.result.mediaType)
                            }
                            is SearchResult.SeerrPersonResult -> {
                                onNavigateToSeerrPersonDetails(result.result.id)
                            }
                        }
                    },
                )
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { if (!isEditing) viewModel.refresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when (val currentState = state) {
                        is CombinedDashboardState.Initial -> {}
                        is CombinedDashboardState.Loading -> {
                            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                        is CombinedDashboardState.Success -> {
                            if (cards.isEmpty()) {
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = mokoString(MR.strings.empty_library),
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    Text(
                                        text = mokoString(MR.strings.empty_dashboard_message),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                    )
                                    Button(
                                        onClick = { showAddCardSheet = true },
                                        modifier = Modifier.padding(top = 16.dp),
                                    ) {
                                        Text(mokoString(MR.strings.add))
                                    }
                                }
                            } else {
                                val reorderableGridState =
                                    rememberReorderableLazyStaggeredGridState(gridState) { from, to ->
                                        val newOrder =
                                            cards.toMutableList().apply {
                                                this[to.index] =
                                                    this[from.index].also {
                                                        this[from.index] = this[to.index]
                                                    }
                                            }
                                        viewModel.saveCardOrder(newOrder)
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                                    }
                                LazyVerticalStaggeredGrid(
                                    state = gridState,
                                    columns = StaggeredGridCells.Fixed(count = if (isCompact) 1 else 2),
                                    verticalItemSpacing = 16.dp,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding =
                                        PaddingValues(
                                            all = 16.dp,
                                            bottom = 16.dp + navigationBarBottomInset(),
                                        ),
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    items(cards, key = { it }) { dashboardCard ->
                                        ReorderableItem(reorderableGridState, key = dashboardCard) { isDragging ->
                                            val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                                            val innerPadding by animateDpAsState(if (isEditing) 4.dp else 0.dp)

                                            Box(contentAlignment = Alignment.Center) {
                                                val cardOnClick: (() -> Unit)? =
                                                    when (dashboardCard) {
                                                        DashboardCards.ArrOverview -> {
                                                            { onNavigateToSettings() }
                                                        }
                                                        DashboardCards.SeerrOverview,
                                                        DashboardCards.PendingRequests,
                                                        DashboardCards.PendingIssues,
                                                        -> {
                                                            { onNavigateToRequestsTab() }
                                                        }
                                                        DashboardCards.ProwlarrOverview -> {
                                                            { onNavigateToProwlarrTab() }
                                                        }
                                                        DashboardCards.DownloadClients -> {
                                                            { onNavigateToDownloadsTab() }
                                                        }
                                                        DashboardCards.ActivityQueue -> {
                                                            { onNavigateToActivityTab() }
                                                        }
                                                        DashboardCards.OnToday,
                                                        DashboardCards.UpcomingReleases,
                                                        -> {
                                                            { onNavigateToScheduleTab() }
                                                        }
                                                        DashboardCards.BazarrOverview -> {
                                                            { onNavigateToBazarrTab() }
                                                        }
                                                        else -> null
                                                    }

                                                Surface(
                                                    shadowElevation = elevation,
                                                    modifier =
                                                        Modifier
                                                            .padding(innerPadding)
                                                            .clip(MaterialTheme.shapes.large)
                                                            .combinedClickable(
                                                                enabled = !isEditing,
                                                                onClick = { cardOnClick?.invoke() },
                                                            ).longPressDraggableHandle(
                                                                onDragStarted = {
                                                                    if (!isEditing) {
                                                                        viewModel.toggleEditing()
                                                                    }
                                                                    hapticFeedback.performHapticFeedback(
                                                                        HapticFeedbackType.GestureThresholdActivate,
                                                                    )
                                                                },
                                                                onDragStopped = {
                                                                    hapticFeedback.performHapticFeedback(
                                                                        HapticFeedbackType.GestureEnd,
                                                                    )
                                                                },
                                                                enabled = true,
                                                            ),
                                                ) {
                                                    DashboardCardContent(
                                                        cardType = dashboardCard,
                                                        currentState = currentState,
                                                        isEditing = isEditing,
                                                        onNavigateToArrDashboard = onNavigateToArrDashboard,
                                                        onNavigateToMediaDetails = onNavigateToMediaDetails,
                                                        onRequestClick = { selectedRequestForSheet = it },
                                                        onIssueClick = { selectedIssueForSheet = it },
                                                        onRequestActivityItem = { selectedActivityItem = it },
                                                        onHealthClick = { showHealthNoticesSheet = true },
                                                        onSeerrRequestsStatClick = {
                                                            showSeerrRequestsSheet = true
                                                        },
                                                        onSeerrIssuesStatClick = {
                                                            showSeerrIssuesSheet = true
                                                        },
                                                    )
                                                }
                                                if (isEditing) {
                                                    Box(
                                                        modifier =
                                                            Modifier
                                                                .align(Alignment.TopEnd)
                                                                .clip(CircleShape)
                                                                .clickable {
                                                                    viewModel.removeCard(dashboardCard)
                                                                }.size(24.dp)
                                                                .background(ArrRed),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Close,
                                                            null,
                                                            tint = Color.Black,
                                                            modifier = Modifier.size(18.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showAddCardSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showAddCardSheet = false },
                        sheetState = sheetState,
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                        ) {
                            Text(
                                text = mokoString(MR.strings.add_dashboard_cards),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = 24.dp),
                            )
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(count = if (isCompact) 1 else 2),
                                verticalItemSpacing = 16.dp,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(all = 16.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                items(availableCards) { card ->
                                    Box(
                                        modifier =
                                            Modifier
                                                .clip(MaterialTheme.shapes.large)
                                                .clickable {
                                                    viewModel.addCard(card)
                                                },
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            Text(
                                                text = mokoString(card.title),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            DashboardCardContent(
                                                enabled = false,
                                                cardType = card,
                                                currentState = CombinedDashboardState.Mock,
                                                isEditing = false,
                                                onNavigateToArrDashboard = onNavigateToArrDashboard,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                selectedRequestForSheet?.let { pkg ->
                    pkg.details?.let { details ->
                        SeerrViewRequestSheet(
                            details = details,
                            serviceDetails = pkg.serviceDetails,
                            requestInProgress = false,
                            requestOverride = pkg.request,
                            onDismissRequest = { selectedRequestForSheet = null },
                            onApproveRequest = { id, profileId, rootFolder, lang, seasons ->
                                viewModel.approveRequest(id, profileId, rootFolder, lang, seasons)
                                selectedRequestForSheet = null
                            },
                            onDeclineRequest = { id ->
                                viewModel.declineRequest(id)
                                selectedRequestForSheet = null
                            },
                            onViewMedia = { tmdbId, type ->
                                selectedRequestForSheet = null
                                onNavigateToSeerrMediaDetails(tmdbId, type)
                            },
                        )
                    }
                }

                selectedIssueForSheet?.let { issuePackage ->
                    IssueDetailsSheet(
                        ip = issuePackage,
                        onDismiss = { selectedIssueForSheet = null },
                        onIssueClosed = {
                            selectedIssueForSheet = null
                            viewModel.refresh()
                        },
                    )
                }

                selectedActivityItem?.let { item ->
                    QueueItemInfoSheet(
                        item = item,
                        onDismiss = { selectedActivityItem = null },
                        onRemove = { showConfirmRemoveActivity = true },
                    )
                }

                if (showConfirmRemoveActivity && selectedActivityItem != null) {
                    ConfirmDeleteItemSheet(
                        onDismiss = { showConfirmRemoveActivity = false },
                        deleteInProgress = removeItemStatus is OperationStatus.InProgress,
                        onDelete = { clientRemove, blocklist, skipRedownload ->
                            viewModel.removeQueueItem(selectedActivityItem!!, clientRemove, blocklist, skipRedownload)
                        },
                    )
                }

                if (showHealthNoticesSheet && state is CombinedDashboardState.Success) {
                    HealthNoticesSheet(
                        instances = (state as CombinedDashboardState.Success).instances,
                        onDismiss = { showHealthNoticesSheet = false },
                    )
                }

                if (showSeerrRequestsSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSeerrRequestsSheet = false },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Text(
                                text = mokoString(MR.strings.requests),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            )
                            RequestsList(
                                items = requestsState.items.filterIsInstance<MediaRequestPackage>(),
                                hasMore = requestsState.hasMore,
                                isLoadingMore = requestsState.isLoadingMore,
                                userState = userState,
                                operationsState = operationsState,
                                onApprove = { requestsViewModel.approveRequest(it) },
                                onDecline = { requestsViewModel.declineRequest(it) },
                                onEdit = {},
                                onDelete = { requestsViewModel.cancelRequest(it) },
                                onRemoveFromService = { requestsViewModel.deleteMediaFile(it) },
                                onNavigateToDetails = { tmdbId, type ->
                                    showSeerrRequestsSheet = false
                                    onNavigateToSeerrMediaDetails(tmdbId, type)
                                },
                                onLoadMore = { requestsViewModel.loadNextRequestsPage() },
                                onViewRequest = { pkg ->
                                    selectedRequestForSheet = pkg
                                },
                            )
                        }
                    }
                }

                if (showSeerrIssuesSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showSeerrIssuesSheet = false },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Text(
                                text = mokoString(MR.strings.issues),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            )
                            IssuesList(
                                items = issuesState.items,
                                hasMore = issuesState.hasMore,
                                isLoadingMore = issuesState.isLoadingMore,
                                onLoadMore = { requestsViewModel.loadNextIssuesPage() },
                                onSelectIssue = { issuePkg ->
                                    selectedIssueForSheet = issuePkg
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
