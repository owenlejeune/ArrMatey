package com.dnfapps.arrmatey.ui.tabs.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.WantedMediaItem
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.ui.components.EmptyWantedState
import com.dnfapps.arrmatey.ui.components.SlidableWantedItem
import com.dnfapps.arrmatey.ui.components.WantedMediaItemView
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.utils.mokoString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WantedTabContent(
    items: List<WantedMediaItem>,
    instances: List<Instance>,
    isLoading: Boolean,
    hasLoaded: Boolean,
    listState: LazyListState,
    onRefresh: () -> Unit,
    onSearchAll: () -> Unit,
    onManualImport: () -> Unit,
    onAutomaticSearch: (WantedMediaItem) -> Unit,
    onInteractiveSearch: (WantedMediaItem) -> Unit,
    modifier: Modifier = Modifier,
    isManualImportEnabled: Boolean = true,
) {
    if (instances.isNotEmpty() && (!hasLoaded && isLoading)) {
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
            if (items.isEmpty()) {
                EmptyWantedState(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                )
            } else {
                LazyColumn(
                    state = listState,
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
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = onSearchAll,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(mokoString(MR.strings.search_all))
                            }
                            OutlinedButton(
                                onClick = onManualImport,
                                modifier = Modifier.weight(1f),
                                enabled = isManualImportEnabled,
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(mokoString(MR.strings.manual_import))
                            }
                        }
                    }

                    items(
                        items = items,
                        key = { it.id ?: 0L },
                    ) { item ->
                        SlidableWantedItem(
                            onAutomaticSearch = { onAutomaticSearch(item) },
                            onInteractiveSearch = { onInteractiveSearch(item) },
                        ) {
                            WantedMediaItemView(
                                item = item,
                            )
                        }
                    }
                }
            }
        }
    }
}
