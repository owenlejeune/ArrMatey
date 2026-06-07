package com.dnfapps.arrmatey.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrHealthType
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.arr.state.ArrInstanceDashboardState
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.arr.viewmodel.CombinedDashboardViewModel
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrHealthCard
import com.dnfapps.arrmatey.ui.components.DiskSpaceSection
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import dev.icerock.moko.resources.compose.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CombinedDashboard(
    viewModel: CombinedDashboardViewModel = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.dashboard)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = { NavigationDrawerButton() }
            )
        }
    ) { contentPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            when (val currentState = state) {
                is CombinedDashboardState.Initial -> {}
                is CombinedDashboardState.Loading -> {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CombinedDashboardState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                            .padding(bottom = navigationBarBottomInset()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OverviewHeader(currentState)

                        if (currentState.activeDownloads.isNotEmpty() || currentState.downloadTransfers.isNotEmpty()) {
                            DownloadsSection(currentState)
                        }

                        if (currentState.calendarItems.isNotEmpty()) {
                            TodaySection(currentState)
                        }

                        Text(
                            text = mokoString(MR.strings.instances),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        currentState.instances.forEach { instanceState ->
                            InstanceDashboardCard(instanceState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewHeader(state: CombinedDashboardState.Success) {
    val totalSize = state.instances.sumOf { it.sizeOnDisk }
    val totalIssues = state.instances.sumOf { it.healthItems.size }
    val criticalIssues = state.instances.sumOf { it.healthItems.count { h -> h.type == ArrHealthType.Error } }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Storage,
            label = mokoString(MR.strings.total_space),
            value = totalSize.bytesAsFileSizeString(),
            color = MaterialTheme.colorScheme.primaryContainer
        )

        StatCard(
            modifier = Modifier.weight(1f),
            icon = if (totalIssues > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
            label = mokoString(MR.strings.health),
            value = if (totalIssues == 0) mokoString(MR.strings.no_issues) else "$totalIssues Issues",
            color = when {
                criticalIssues > 0 -> MaterialTheme.colorScheme.errorContainer
                totalIssues > 0 -> Color(0xffffc653).copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DownloadsSection(state: CombinedDashboardState.Success) {
    val totalDownloadSpeed = state.downloadTransfers.sumOf { it.downloadSpeed }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(mokoString(MR.strings.downloads), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (totalDownloadSpeed > 0) {
                    Text(
                        "${totalDownloadSpeed.bytesAsFileSizeString()}/s",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            state.activeDownloads.take(3).forEach { download ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            download.name,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${(download.progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                    }
                    LinearProgressIndicator(
                        progress = { download.progress.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    )
                }
            }
            
            if (state.activeDownloads.size > 3) {
                Text(
                    mokoString(MR.strings.additional_episodes_count, state.activeDownloads.size - 3),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun TodaySection(state: CombinedDashboardState.Success) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(mokoString(MR.strings.today), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            state.calendarItems.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val title = when (item) {
                        is Episode -> item.series?.title ?: ""
                        is EpisodeGroup -> item.first.series?.title ?: ""
                        is ArrAlbum -> item.artist?.title ?: ""
                        is ArrMovie -> item.title ?: ""
                        is Audiobook -> item.title ?: ""
                        is Book -> item.title
                    }
                    val sub = when (item) {
                        is Episode -> "${item.seasonEpLabel}: ${item.title ?: ""}"
                        is EpisodeGroup -> {
                            val episodes = listOf(item.first) + item.additional
                            episodes.joinToString(", ") { "${it.seasonEpLabel}: ${it.title ?: ""}" }
                        }
                        is ArrAlbum -> item.title ?: ""
                        is ArrMovie -> {
                            val date = item.releaseDate ?: item.digitalRelease ?: item.physicalRelease ?: item.inCinemas
                            val label = when (date) {
                                item.physicalRelease -> mokoString(MR.strings.physical_release)
                                item.digitalRelease -> mokoString(MR.strings.digital_release)
                                item.inCinemas -> mokoString(MR.strings.in_cinemas)
                                else -> mokoString(MR.strings.release_date)
                            }
                            label
                        }
                        else -> ""
                    }
                    
                    Box(Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Column {
                        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        if (sub.isNotBlank()) {
                            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstanceDashboardCard(state: ArrInstanceDashboardState) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(state.instance.type.icon),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.instance.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.totalItems} Items • ${state.sizeOnDisk.bytesAsFileSizeString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (state.healthItems.any { it.type == ArrHealthType.Error }) {
                    Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                } else if (state.healthItems.isNotEmpty()) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xffffc653), modifier = Modifier.size(20.dp))
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.healthItems.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Text(
                            text = mokoString(MR.strings.health),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.healthItems.forEach {
                                ArrHealthCard(it)
                            }
                        }
                    }

                    if (state.disks.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Text(
                            text = mokoString(MR.strings.disk_space),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        DiskSpaceSection(state.disks)
                    }
                    
                    state.softwareStatus?.version?.let { version ->
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Row {
                            Text(mokoString(MR.strings.version), style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.weight(1f))
                            Text(version, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
