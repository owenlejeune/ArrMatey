package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.compose.SeerrTab
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.seerrNavigator
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.screens.requests.IssuesContent
import com.dnfapps.arrmatey.ui.screens.requests.RequestsContent
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RequestsScreen(
    viewModel: RequestsViewModel,
    isExpanded: Boolean = false,
    wideRailIsVisible: Boolean = false,
    instancesViewModel: InstancesViewModel = koinInjectParams(InstanceType.Seerr)
) {
    val navigation = seerrNavigator
    val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val pagedData by viewModel.requestsState.collectAsStateWithLifecycle()
    val issuesData by viewModel.issuesState.collectAsStateWithLifecycle()
    val requestOperationsState by viewModel.operationsState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.seerr)) },
                navigationIcon = {
                    if (!wideRailIsVisible) {
                        NavigationDrawerButton()
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = pagedData.isLoading && pagedData.items.isNotEmpty(),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (instancesState.selectedInstance == null) {
                NoInstanceView(InstanceType.Seerr)
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (!isExpanded) {
                        SecondaryTabRow(
                            selectedTabIndex = SeerrTab.entries.indexOf(selectedTab)
                        ) {
                            Tab(
                                selected = selectedTab == SeerrTab.Requests,
                                onClick = { viewModel.setSelectedTab(SeerrTab.Requests) },
                                text = {
                                    Text(text = buildString {
                                        append(mokoString(MR.strings.requests))
                                        if (pagedData.totalItemCount > 0) {
                                            append(" (${pagedData.totalItemCount})")
                                        }
                                    })
                                }
                            )
                            Tab(
                                selected = selectedTab == SeerrTab.Issues,
                                onClick = { viewModel.setSelectedTab(SeerrTab.Issues) },
                                text = {
                                    Text(text = buildString {
                                        append(mokoString(MR.strings.issues))
                                        if (issuesData.totalItemCount > 0) {
                                            append(" (${issuesData.totalItemCount})")
                                        }
                                    })
                                }
                            )
                        }
                    } else {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = mokoString(MR.strings.requests),
                                    style = MaterialTheme.typography.titleMediumEmphasized,
                                    modifier = Modifier.weight(1f).padding(horizontal = 24.dp)
                                )
                                Text(
                                    text = mokoString(MR.strings.issues),
                                    style = MaterialTheme.typography.titleMediumEmphasized,
                                    modifier = Modifier.weight(1f).padding(horizontal = 24.dp)
                                )
                            }
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    RequestsContent(
                                        pagedData = pagedData,
                                        userState = userState,
                                        operationsState = requestOperationsState,
                                        onApprove = { viewModel.approveRequest(it) },
                                        onDecline = { viewModel.declineRequest(it) },
                                        onEdit = { },
                                        onDelete = { viewModel.cancelRequest(it) },
                                        onRemoveFromService = { viewModel.deleteMediaFile(it) },
                                        onNavigateToDetails = { tmdbId, type ->
                                            navigation.toDetails(tmdbId, type)
                                        },
                                        onLoadMore = { viewModel.loadNextRequestsPage() },
                                        onRetry = { viewModel.retryRequests() },
                                        onClearError = { viewModel.clearRequestsError() }
                                    )
                                }
                                VerticalDivider(modifier = Modifier.padding(vertical = 24.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    IssuesContent(
                                        pagedData = issuesData,
                                        onLoadMore = { viewModel.loadNextIssuesPage() },
                                        onRetry = { viewModel.retryIssues() },
                                        onClearError = { viewModel.clearIssuesError() }
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