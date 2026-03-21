package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.state.RequestOperationsState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RequestsContent(
    pagedData: PagedData<MediaRequestPackage>,
    userState: SeerrUser?,
    operationsState: RequestOperationsState,
    onApprove: (Long) -> Unit,
    onDecline: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onRemoveFromService: (MediaRequest) -> Unit,
    onNavigateToDetails: (Long, RequestType) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            pagedData.isLoading && pagedData.items.isEmpty() -> {
                LoadingIndicator(modifier = Modifier.size(96.dp).align(Alignment.Center))
            }

            pagedData.isEmpty -> {
                EmptyRequestsState(
                    message = mokoString(MR.strings.no_requests_found),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                RequestsList(
                    items = pagedData.items,
                    hasMore = pagedData.hasMore,
                    isLoadingMore = pagedData.isLoadingMore,
                    userState = userState,
                    operationsState = operationsState,
                    onApprove = onApprove,
                    onDecline = onDecline,
                    onEdit = onEdit,
                    onDelete = onDelete,
                    onRemoveFromService = onRemoveFromService,
                    onNavigateToDetails = onNavigateToDetails,
                    onLoadMore = onLoadMore
                )
            }
        }

        pagedData.error?.let { error ->
            ErrorBanner(
                error = error,
                onRetry = onRetry,
                onDismiss = onClearError,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}