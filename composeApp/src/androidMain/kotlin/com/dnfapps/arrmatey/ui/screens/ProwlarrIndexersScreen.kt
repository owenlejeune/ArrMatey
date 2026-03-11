package com.dnfapps.arrmatey.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.IndexerMessageType
import com.dnfapps.arrmatey.arr.api.model.IndexerStatus
import com.dnfapps.arrmatey.arr.api.model.ProwlarrIndexer
import com.dnfapps.arrmatey.arr.api.model.ReleaseProtocol
import com.dnfapps.arrmatey.arr.state.ProwlarrIndexersState
import com.dnfapps.arrmatey.arr.viewmodel.ProwlarrIndexersViewModel
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ContainerCard
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun ProwlarrIndexersContent(
    modifier: Modifier = Modifier,
    viewModel: ProwlarrIndexersViewModel
) {
    val indexersState by viewModel.indexers.collectAsStateWithLifecycle()
    val indexersStatus by viewModel.indexerStatus.collectAsStateWithLifecycle()

    var showIndexerStatus by remember { mutableStateOf<IndexerStatus?>(null) }

    Column(
        modifier = modifier.padding(horizontal = 12.dp)
    ) {
        when (val state = indexersState) {
            is ProwlarrIndexersState.Initial,
            is ProwlarrIndexersState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProwlarrIndexersState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.message,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            is ProwlarrIndexersState.Success -> {
                if (state.items.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = mokoString(MR.strings.no_indexers_configured),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item { Spacer(modifier = Modifier.size(4.dp)) }
                            items(items = state.items, key = { it.id }) { indexer ->
                                val indexerStatus =
                                    indexersStatus.firstOrNull { it.indexerId == indexer.id }
                                IndexerCard(
                                    indexer = indexer,
                                    hasIssues = indexerStatus?.hasFailure ?: false,
                                    onShowIssues = { showIndexerStatus = indexerStatus }
                                )
                            }
                            item { Spacer(modifier = Modifier.size(4.dp)) }
                        }
                    }
                }
            }
        }
    }

    showIndexerStatus?.let { indexersStatus ->
        ModalBottomSheet(
            onDismissRequest = { showIndexerStatus = null }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                indexersStatus.disabledTill?.let { disabledTill ->
                    ContainerCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = mokoString(MR.strings.disabled_until),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = disabledTill.format ()
                        )
                    }
                }
                indexersStatus.mostRecentFailure?.let { mostRecentFailure ->
                    ContainerCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = mokoString(MR.strings.most_recent_failure),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = mostRecentFailure.format ()
                        )
                    }
                }
                indexersStatus.initialFailure?.let { initialFailure ->
                    ContainerCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = mokoString(MR.strings.initial_failure),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = initialFailure.format()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IndexerCard(
    indexer: ProwlarrIndexer,
    hasIssues: Boolean,
    onShowIssues: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onShowIssues
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = indexer.name ?: indexer.implementationName ?: mokoString(MR.strings.unknown),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                val protocol = indexer.protocol?.name ?: mokoString(MR.strings.unknown).lowercase()
                val protocolColor = when (indexer.protocol) {
                    ReleaseProtocol.Torrent -> MaterialTheme.colorScheme.primary
                    ReleaseProtocol.Usenet -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.outline
                }
                Text(
                    text = protocol,
                    style = MaterialTheme.typography.labelMedium,
                    color = protocolColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val dotColor = if (indexer.enable)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )

                Text(
                    text = if (indexer.enable) mokoString(MR.strings.enabled) else mokoString(MR.strings.disabled),
                    style = MaterialTheme.typography.bodySmall,
                    color = dotColor
                )

                if (indexer.supportsRss) {
                    Icon(
                        imageVector = Icons.Default.RssFeed,
                        contentDescription = "RSS",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (indexer.supportsSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = mokoString(MR.strings.search),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.weight(1f))
                if (hasIssues) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = ArrOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            indexer.message?.message?.let { msg ->
                if (msg.isNotBlank()) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (indexer.message?.type) {
                            IndexerMessageType.Warning -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}
