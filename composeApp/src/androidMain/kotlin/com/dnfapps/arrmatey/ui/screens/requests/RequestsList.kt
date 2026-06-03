package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.state.RequestOperationsState

@Composable
fun RequestsList(
    items: List<MediaRequestPackage>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    userState: SeerrUser?,
    operationsState: RequestOperationsState,
    onApprove: (Long) -> Unit,
    onDecline: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onRemoveFromService: (MediaRequest) -> Unit,
    onNavigateToDetails: (Long, RequestType) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.collect { lastVisibleIndex ->
            if (lastVisibleIndex != null &&
                lastVisibleIndex >= items.size - 3 &&
                hasMore &&
                !isLoadingMore) {
                onLoadMore()
            }
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = items,
            key = { it.request.id }
        ) { rPackage ->
            RequestCard(
                mediaPackage = rPackage,
                user = userState,
                requestOperationsState = operationsState,
                onApproveClicked = { onApprove(rPackage.request.id) },
                onDeclineClicked = { onDecline(rPackage.request.id) },
                onEditClicked = { onEdit(rPackage.request.id) },
                onDeleteClicked = { onDelete(rPackage.request.id) },
                onRemoveFromServiceClicked = { onRemoveFromService(rPackage.request) },
                onClick = {
                    onNavigateToDetails(
                        rPackage.request.media.tmdbId,
                        rPackage.request.type
                    )
                }
            )
        }

        if (isLoadingMore) {
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