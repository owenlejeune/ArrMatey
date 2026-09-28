package com.dnfapps.arrmatey.ui.tabs.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.ui.components.ActivityItem
import com.dnfapps.arrmatey.ui.components.EmptyActivityState
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActiveQueueTabContent(
    queueItems: List<QueueItem>,
    instances: List<Instance>,
    isLoading: Boolean,
    hasLoaded: Boolean,
    useColoredCards: Boolean,
    onRefresh: () -> Unit,
    onSelectItem: (QueueItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (instances.isNotEmpty() && (!hasLoaded || (queueItems.isEmpty() && isLoading))) {
        LoadingIndicator(
            modifier = Modifier.size(96.dp),
        )
    } else {
        PullToRefreshBox(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            isRefreshing = isLoading,
            onRefresh = onRefresh,
        ) {
            if (queueItems.isEmpty()) {
                EmptyActivityState(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp + LocalFloatingBarBottomPadding.current,
                    ),
                ) {
                    items(items = queueItems) { item ->
                        ActivityItem(
                            item = item,
                            useFullColorCards = useColoredCards,
                        ) {
                            onSelectItem(item)
                        }
                    }
                    item {
                        Spacer(Modifier.height(LocalFloatingBarBottomPadding.current + 16.dp))
                    }
                }
            }
        }
    }
}
