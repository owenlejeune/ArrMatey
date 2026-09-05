package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.state.RequestOperationsState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.sheets.SeerrViewRequestSheet
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
    onClearError: () -> Unit,
    onApproveWithDetails: (Long, Long?, String?, Long?, List<Int>?) -> Unit = { id, _, _, _, _ -> onApprove(id) },
) {
    var selectedRequestPackageForSheet by remember { mutableStateOf<MediaRequestPackage?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            pagedData.isLoading && pagedData.items.isEmpty() -> {
                LoadingIndicator(modifier = Modifier.size(96.dp).align(Alignment.Center))
            }

            pagedData.isEmpty -> {
                EmptyRequestsState(
                    message = mokoString(MR.strings.no_requests_found),
                    modifier = Modifier.align(Alignment.Center),
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
                    onLoadMore = onLoadMore,
                    onViewRequest = { pkg -> selectedRequestPackageForSheet = pkg },
                )
            }
        }

        pagedData.error?.let { error ->
            ErrorBanner(
                error = error,
                onRetry = onRetry,
                onDismiss = onClearError,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
            )
        }
    }

    selectedRequestPackageForSheet?.let { pkg ->
        pkg.details?.let { details ->
            SeerrViewRequestSheet(
                details = details,
                serviceDetails = pkg.serviceDetails,
                requestInProgress = operationsState.approvalStates[pkg.request.id] == OperationStatus.InProgress,
                requestOverride = pkg.request,
                onDismissRequest = { selectedRequestPackageForSheet = null },
                onApproveRequest = { id, profileId, rootFolder, languageProfileId, seasons ->
                    onApproveWithDetails(id, profileId, rootFolder, languageProfileId, seasons)
                    selectedRequestPackageForSheet = null
                },
                onDeclineRequest = { id ->
                    onDecline(id)
                    selectedRequestPackageForSheet = null
                },
            )
        }
    }
}
