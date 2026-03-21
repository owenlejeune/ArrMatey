package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SeerrScreen
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.screens.requests.*
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RequestsScreen(
    viewModel: RequestsViewModel,
    instancesViewModel: InstancesViewModel = koinInjectParams(InstanceType.Seerr),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SeerrScreen> = navigationManager.requests()
) {
    val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val pagedData by viewModel.requestsState.collectAsStateWithLifecycle()
    val requestOperationsState by viewModel.operationsState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.requests)) },
                navigationIcon = { NavigationDrawerButton() }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = pagedData.isLoading && pagedData.items.isEmpty(),
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (instancesState.selectedInstance == null) {
                NoInstanceView(InstanceType.Seerr)
            } else {
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
                        navigation.navigateTo(SeerrScreen.Details(tmdbId, type))
                    },
                    onLoadMore = { viewModel.loadNextPage() },
                    onRetry = { viewModel.retry() },
                    onClearError = { viewModel.clearError() }
                )
            }
        }
    }
}