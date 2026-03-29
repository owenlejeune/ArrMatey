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
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IssuesContent(
    pagedData: PagedData<MediaIssuePackage>,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    var selectedIssue by remember { mutableStateOf<MediaIssuePackage?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            pagedData.isLoading && pagedData.items.isEmpty() -> {
                LoadingIndicator(modifier = Modifier.size(96.dp).align(Alignment.Center))
            }

            pagedData.isEmpty -> {
                EmptyIssuesState(
                    message = mokoString(MR.strings.no_issues_found),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                IssuesList(
                    items = pagedData.items,
                    hasMore = pagedData.hasMore,
                    isLoadingMore = pagedData.isLoadingMore,
                    onLoadMore = onLoadMore,
                    onSelectIssue = {
                        selectedIssue = it
                    }
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

    selectedIssue?.let { issuePackage ->
        IssueDetailsSheet(
            ip = issuePackage,
            onDismiss = { selectedIssue = null }
        )
    }
}