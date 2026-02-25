package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.entensions.collectAsLazyPagingItems
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestStatus
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.ui.components.NoInstanceView
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.utils.koinInjectParams

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RequestsScreen(
    viewModel: RequestsViewModel,
    instancesViewModel: InstancesViewModel = koinInjectParams(InstanceType.Seerr)
) {
    val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()
    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val requestsPagingState = viewModel.requestsState.collectAsLazyPagingItems(
        onLoadMore = { viewModel.loadNextPage() },
        onRefresh = { viewModel.refresh() },
        prefetchDistance = 2
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Requests") },
                navigationIcon = { NavigationDrawerButton() }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = requestsPagingState.isLoading,
            onRefresh = { requestsPagingState.refresh() },
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (instancesState.selectedInstance == null) {
                NoInstanceView(InstanceType.Seerr)
            } else {
                when {
                    requestsPagingState.isLoading && requestsPagingState.itemCount == 0 -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator(
                                modifier = Modifier.size(96.dp)
                            )
                        }
                    }

                    requestsPagingState.isEmpty -> {
                        EmptyState(
                            message = "No requests found",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Column {
                                    userState?.let {
                                        Text(it.id.toString())
                                        Text(it.displayName)
                                        Text(it.plexId?.toString() ?: "")
                                    }
                                }
                            }
                            items(
                                count = requestsPagingState.itemCount,
                                key = { index -> requestsPagingState.peek(index)?.request?.id ?: index }
                            ) { index ->
                                requestsPagingState[index]?.let { request ->
                                    RequestCard(mediaPackage = request)
                                }
                            }

                            if (requestsPagingState.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }

                requestsPagingState.error?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                TextButton(onClick = { requestsPagingState.retry() }) {
                                    Text("Retry")
                                }
                                IconButton(onClick = { viewModel.clearError() }) {
                                    Icon(Icons.Default.Close, "Dismiss")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestCard(mediaPackage: MediaRequestPackage) {
    val request = mediaPackage.request
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Has details - ${mediaPackage.details != null}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (request.isMovie) Icons.Default.Movie else Icons.Default.Tv,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = if (request.isMovie) "Movie" else "TV Show",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (request.is4k) {
                        AssistChip(
                            onClick = { },
                            label = { Text("4K") },
                            leadingIcon = {
                                Icon(Icons.Default.HighQuality, null)
                            }
                        )
                    }
                }

                StatusChip(status = RequestStatus.fromValue(request.status))
            }

            Text(
                text = "Requested by ${request.requestedBy.displayName}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (request.isTv && request.seasonCount > 0) {
                Text(
                    text = "${request.seasonCount} season${if (request.seasonCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "TMDB ID: ${request.media.tmdbId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusChip(status: RequestStatus) {
    val (color, icon) = when (status) {
        RequestStatus.Pending -> MaterialTheme.colorScheme.tertiary to Icons.Default.Schedule
        RequestStatus.Approved -> MaterialTheme.colorScheme.primary to Icons.Default.CheckCircle
        RequestStatus.Declined -> MaterialTheme.colorScheme.error to Icons.Default.Cancel
        RequestStatus.Available -> MaterialTheme.colorScheme.secondary to Icons.Default.CloudDone
        RequestStatus.PartiallyAvailable -> MaterialTheme.colorScheme.secondary to Icons.Default.CloudSync
    }

    AssistChip(
        onClick = { },
        label = { Text(status.name.replace("_", " ")) },
        leadingIcon = { Icon(icon, null) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.2f),
            labelColor = color,
            leadingIconContentColor = color
        )
    )
}

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
