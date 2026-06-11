package com.dnfapps.arrmatey.ui.screens.dashboard

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
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
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.arr.viewmodel.CombinedDashboardViewModel
import com.dnfapps.arrmatey.compose.DashboardCards
import com.dnfapps.arrmatey.entensions.PaddingValues
import com.dnfapps.arrmatey.navigation.DashboardScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.dashboardNavigator
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.navigation.openArrDashboard
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.theme.ArrRed
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyStaggeredGridState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CombinedDashboard(
    windowSizeClass: WindowSizeClass,
    viewModel: CombinedDashboardViewModel = koinInject()
) {
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val isEditing by viewModel.isEditing.collectAsStateWithLifecycle()
    val availableCards = remember(cards) { DashboardCards.entries.filter { it !in cards } }

    val gridState = rememberLazyStaggeredGridState()

    var showAddCardSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        modifier = if (isCompact) {
            Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        } else Modifier,
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.dashboard)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (isEditing) {
                        IconButton(onClick = { viewModel.toggleEditing() }) {
                            Icon(Icons.Default.Close, null)
                        }
                    } else if (isCompact) NavigationDrawerButton()
                },
                windowInsets = TopAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            viewModel.resetCardsOrder()
                            scope.launch {
                                gridState.animateScrollToItem(0)
                            }
                        }) {
                            Icon(Icons.Default.Restore, null)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isEditing && availableCards.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showAddCardSheet = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(mokoString(MR.strings.add)) }
                )
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { contentPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            enabled = !isEditing,
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            when (val currentState = state) {
                is CombinedDashboardState.Initial -> {}
                is CombinedDashboardState.Loading -> {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CombinedDashboardState.Success -> {
                    if (cards.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = mokoString(MR.strings.empty_library),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = mokoString(MR.strings.empty_dashboard_message),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { showAddCardSheet = true },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text(mokoString(MR.strings.add))
                            }
                        }
                    } else {
                        val reorderableGridState =
                            rememberReorderableLazyStaggeredGridState(gridState) { from, to ->
                                val newOrder = cards.toMutableList().apply {
                                    this[to.index] = this[from.index].also {
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
                            contentPadding = PaddingValues(
                                all = 16.dp,
                                bottom = 16.dp + navigationBarBottomInset()
                            ),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(cards, key = { it }) { dashboardCard ->
                                ReorderableItem(reorderableGridState, key = dashboardCard) { isDragging ->
                                    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                                    val innerPadding by animateDpAsState(if (isEditing) 4.dp else 0.dp)

                                    Box(contentAlignment = Alignment.Center) {
                                        val navManager = navigationManager

                                        val onLongClick = {
                                            if (!isEditing) {
                                                viewModel.toggleEditing()
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }

                                        val cardOnClick: (() -> Unit)? = when (dashboardCard) {
                                            DashboardCards.ArrOverview -> { { navManager.openSettings() } }
                                            DashboardCards.SeerrOverview -> { { navManager.openRequestsTab() } }
                                            DashboardCards.ProwlarrOverview -> { { navManager.openProwlarrTab() } }
                                            DashboardCards.DownloadClients -> { { navManager.openDownloadClientsTab() } }
                                            DashboardCards.ActivityQueue -> { { navManager.openActivityTab() } }
                                            DashboardCards.OnToday -> { { navManager.openScheduleTab() } }
                                            DashboardCards.UpcomingReleases -> { { navManager.openScheduleTab() } }
                                            else -> null
                                        }

                                        Surface(
                                            shadowElevation = elevation,
                                            modifier = Modifier
                                                .padding(innerPadding)
                                                .clip(MaterialTheme.shapes.large)
                                                .combinedClickable(
                                                    enabled = !isEditing,
                                                    onClick = { cardOnClick?.invoke() }
                                                )
                                                .longPressDraggableHandle(
                                                    onDragStarted = {
                                                        if (!isEditing) {
                                                            viewModel.toggleEditing()
                                                        }
                                                        hapticFeedback.performHapticFeedback(
                                                            HapticFeedbackType.GestureThresholdActivate
                                                        )
                                                    },
                                                    onDragStopped = {
                                                        hapticFeedback.performHapticFeedback(
                                                            HapticFeedbackType.GestureEnd
                                                        )
                                                    },
                                                    enabled = true
                                                )
                                        ) {
                                            DashboardCardContent(
                                                cardType = dashboardCard,
                                                currentState = currentState,
                                                isEditing = isEditing
                                            )
                                        }
                                        if (isEditing) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        viewModel.removeCard(dashboardCard)
                                                    }
                                                    .size(24.dp)
                                                    .background(ArrRed),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Close, null,
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(18.dp)
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
    }

    if (showAddCardSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddCardSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = mokoString(MR.strings.add_dashboard_cards),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(count = if (isCompact) 1 else 2),
                    verticalItemSpacing = 16.dp,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(all = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCards) { card ->
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.large)
                                .clickable {
                                    viewModel.addCard(card)
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = mokoString(card.title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                DashboardCardContent(
                                    enabled = false,
                                    cardType = card,
                                    currentState = CombinedDashboardState.Mock,
                                    isEditing = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCardContent(
    cardType: DashboardCards,
    currentState: CombinedDashboardState.Success,
    isEditing: Boolean,
    enabled: Boolean = true,
    navManager: NavigationManager = navigationManager,
    navigator: Navigator<DashboardScreen> = dashboardNavigator
) {
    when (cardType) {
        DashboardCards.ArrOverview ->
            DashboardOverviewCards(
                state = currentState,
                isEditing = isEditing
            )

        DashboardCards.SeerrOverview ->
            SeerrSection(
                state = currentState,
                isEditing = isEditing
            )

        DashboardCards.ProwlarrOverview ->
            DashboardProwlarrSection(
                state = currentState,
                isEditing = isEditing
            )

        DashboardCards.Network ->
            DashboardNetworkSection(currentState)

        DashboardCards.RecentlyAdded ->
            RecentlyAddedSection(
                enabled = !isEditing && enabled,
                state = currentState,
                onOpenItem = { id, type ->
                    navManager.arr(type).toDetails(id)
                }
            )

        DashboardCards.DownloadClients ->
            DashboardDownloadClientsSection(
                state = currentState,
                isEditing = isEditing
            )

        DashboardCards.ActivityQueue ->
            DashboardActivityQueueSection(
                state = currentState,
                isEditing = isEditing
            )

        DashboardCards.OnToday ->
            DashboardTodaySection(
                state = currentState,
                isEditing = isEditing
            )

        DashboardCards.UpcomingReleases ->
            DashboardUpcomingSection(
                state = currentState,
                isEditing = isEditing
            )

        DashboardCards.InstanceDashboard ->
            InstanceDashboardSection(
                state = currentState,
                enabled = !isEditing && enabled,
                onInstanceClicked = { id ->
                    navigator.openArrDashboard(id)
                }
            )
    }
}