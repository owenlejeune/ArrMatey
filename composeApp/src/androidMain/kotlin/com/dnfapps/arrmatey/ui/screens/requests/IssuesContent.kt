package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.client.paging.PagedData
import com.dnfapps.arrmatey.seerr.api.model.IssueState
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IssuesContent(
    pagedData: PagedData<MediaIssuePackage>,
    selectedFilter: IssueState,
    onFilterSelected: (IssueState) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    onRefresh: () -> Unit = {},
) {
    var selectedIssue by remember { mutableStateOf<MediaIssuePackage?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(IssueState.entries) { state ->
                FilterChip(
                    selected = selectedFilter == state,
                    onClick = { onFilterSelected(state) },
                    label = { Text(mokoString(state.resource)) },
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                pagedData.isLoading && pagedData.items.isEmpty() -> {
                    LoadingIndicator(modifier = Modifier.size(96.dp).align(Alignment.Center))
                }

                pagedData.isEmpty -> {
                    EmptyIssuesState(
                        message = mokoString(MR.strings.no_issues_found),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    IssuesList(
                        items = pagedData.items,
                        hasMore = pagedData.hasMore,
                        isLoadingMore = pagedData.isLoadingMore,
                        selectedFilter = selectedFilter,
                        onLoadMore = onLoadMore,
                        loadMoreFailed = pagedData.loadMoreFailed,
                        onSelectIssue = {
                            selectedIssue = it
                        },
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
    }

    selectedIssue?.let { issuePackage ->
        IssueDetailsSheet(
            ip = issuePackage,
            onDismiss = { selectedIssue = null },
            onIssueClosed = {
                selectedIssue = null
                onRefresh()
            },
        )
    }
}
