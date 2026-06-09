package com.dnfapps.arrmatey.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrHealthType
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.state.ArrInstanceDashboardState
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.arr.state.NetworkStatusState
import com.dnfapps.arrmatey.arr.state.ProwlarrDashboardState
import com.dnfapps.arrmatey.arr.state.SeerrDashboardState
import com.dnfapps.arrmatey.arr.viewmodel.CombinedDashboardViewModel
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.navigation.toDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.ArrHealthCard
import com.dnfapps.arrmatey.ui.components.DiskSpaceItem
import com.dnfapps.arrmatey.ui.components.DiskSpaceSection
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrPurple
import com.dnfapps.arrmatey.ui.theme.ArrRed
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CombinedDashboard(
    viewModel: CombinedDashboardViewModel = koinInject(),
) {
    val navManager = navigationManager
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(mokoString(MR.strings.dashboard)) },
                scrollBehavior = scrollBehavior,
                navigationIcon = { NavigationDrawerButton() },
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

                        currentState.networkStatus?.let {
                            NetworkSection(it)
                        }

                        if (currentState.recentlyAdded.isNotEmpty()) {
                            RecentlyAddedSection(
                                items = currentState.recentlyAdded,
                            ) { media ->
                                val type = when (media) {
                                    is ArrSeries -> InstanceType.Sonarr
                                    is ArrMovie -> InstanceType.Radarr
                                    is Arrtist -> InstanceType.Lidarr
                                    is Author -> InstanceType.Booksehelf
                                    is Audiobook -> InstanceType.Listenarr
                                    else -> null
                                }
                                type?.let {
                                    navManager.arr(it).toDetails(media.id ?: 0)
                                    navManager.navigateToTab(navManager.tabFor(it))
                                }
                            }
                        }

                        if (currentState.downloadClients.isNotEmpty()) {
                            DownloadClientsSection(currentState)
                        }

                        if (currentState.recentActivity.isNotEmpty()) {
                            RecentActivitySection(currentState.recentActivity)
                        }

                        if (currentState.calendarItems.isNotEmpty()) {
                            TodaySection(currentState)
                        }

                        if (currentState.upcomingCalendarItems.isNotEmpty()) {
                            UpcomingSection(currentState)
                        }

                        if (currentState.seerrInstances.isNotEmpty()) {
                            SeerrSection(currentState.seerrInstances)
                        }

                        if (currentState.prowlarrStats.isNotEmpty()) {
                            ProwlarrSection(currentState.prowlarrStats)
                        }

                        StorageSection(currentState.instances)

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
private fun NetworkSection(state: NetworkStatusState) {
    if ((state.instanceStatuses.isEmpty()) && (state.ssid == null)) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = mokoString(MR.strings.network_status),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                state.ssid?.let { ssid ->
                    Text(
                        text = ssid,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (state.instanceStatuses.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.instanceStatuses.forEach { status ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(status.icon),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = status.instanceName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = status.currentEndpoint,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (status.isOnline) ArrGreen.copy(alpha = 0.1f)
                                            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (status.isOnline) mokoString(MR.strings.online)
                                               else mokoString(MR.strings.offline),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (status.isOnline) ArrGreen else MaterialTheme.colorScheme.error
                                    )
                                }

                                if (status.isLocalSwitchingEnabled) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (status.isLocal) ArrBlue.copy(alpha = 0.1f)
                                                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (status.isLocal) mokoString(MR.strings.local_network)
                                                   else mokoString(MR.strings.remote_vpn),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (status.isLocal) ArrBlue else MaterialTheme.colorScheme.primary
                                        )
                                    }
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
private fun RecentlyAddedSection(
    items: List<ArrMedia>,
    onItemClick: (ArrMedia) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    Icons.Default.History, null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = mokoString(MR.strings.recently_added),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    PosterItem(
                        item = item,
                        modifier = Modifier.width(120.dp),
                        onItemClick = onItemClick,
                        showFooter = true
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageSection(instances: List<ArrInstanceDashboardState>) {
    val allDisks = instances.asSequence().flatMap { it.disks }.distinctBy { it.path }.toList()
    val warnings = allDisks.filter { it.usedPercentage >= 0.9f }
    val totalInstanceUsage = instances.sumOf { it.sizeOnDisk }

    if ((totalInstanceUsage > 0) || warnings.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Storage, null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = mokoString(MR.strings.storage_breakdown),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (warnings.isNotEmpty()) {
                    warnings.forEachIndexed { index, disk ->
                        DiskSpaceItem(disk = disk)
                        if ((index < warnings.size - 1) || (totalInstanceUsage > 0)) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                if (totalInstanceUsage > 0) {
                    val activeInstances = instances.asSequence().filter { it.sizeOnDisk > 0 }
                        .sortedByDescending { it.sizeOnDisk }.toList()

                    activeInstances.forEachIndexed { index, instanceState ->
                        InstanceStorageItem(instanceState, totalInstanceUsage)
                        if (index < activeInstances.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun InstanceStorageItem(state: ArrInstanceDashboardState, totalUsage: Long) {
    val completion = if (state.library.isNotEmpty()) {
        state.library.asSequence().map { it.statusProgress }.average().toFloat()
    } else 0f

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(state.instance.type.icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.instance.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.totalItems} Items • ${(completion * 100).toInt()}% Downloaded",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = state.sizeOnDisk.bytesAsFileSizeString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LinearProgressIndicator(
            progress = { if (totalUsage > 0) state.sizeOnDisk.toFloat() / totalUsage.toFloat() else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.background
        )

        AnimatedVisibility(
            visible = expanded && state.disks.isNotEmpty(),
            exit = shrinkVertically(),
            enter = expandVertically()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                state.disks.forEach { disk ->
                    val usedSpace = disk.totalSpace - disk.freeSpace
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = disk.path ?: mokoString(MR.strings.unknown),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${usedSpace.bytesAsFileSizeString()} / ${disk.totalSpace.bytesAsFileSizeString()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${(disk.usedPercentage * 100).toInt()}% full",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (disk.usedPercentage > 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
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
private fun TodaySection(state: CombinedDashboardState.Success) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday, null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = mokoString(MR.strings.today),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.calendarItems.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.nothing_on_today),
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 2.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            state.calendarItems.forEach { item ->
                CalendarItemRow(item)
            }
        }
    }
}

@Composable
private fun UpcomingSection(state: CombinedDashboardState.Success) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday, null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = mokoString(MR.strings.upcoming),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            state.upcomingCalendarItems.take(5).forEach { item ->
                CalendarItemRow(item, showDate = true)
            }
        }
    }
}

@Composable
private fun CalendarItemRow(item: CalendarItem, showDate: Boolean = false) {
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
            if (showDate) {
                val firstDate = item.getCalendarDates().firstOrNull()
                firstDate?.let {
                    val date = it.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    Text(
                        text = date.format("EEE, MMM d"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun InstanceDashboardCard(state: ArrInstanceDashboardState) {
    var expanded by rememberSaveable { mutableStateOf(value = false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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

@Composable
private fun ProwlarrSection(
    prowlarrStats: List<ProwlarrDashboardState>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            prowlarrStats.forEach { state ->
                var expanded by remember { mutableStateOf(false) }
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.clickable { expanded = !expanded }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(state.instance.type.icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = state.instance.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CountStatItem(
                            icon = Icons.Default.Favorite,
                            modifier = Modifier.weight(1f),
                            label = mokoString(MR.strings.healthy_indexers),
                            count = state.healthyIndexers,
                            color = ArrGreen,
                        )
                        CountStatItem(
                            icon = Icons.Default.Error,
                            modifier = Modifier.weight(1f),
                            label = mokoString(MR.strings.failing_indexers),
                            count = state.failingIndexers,
                            color = MaterialTheme.colorScheme.errorContainer
                        )
                    }

                    AnimatedVisibility(
                        visible = expanded && state.failingIndexerNames.isNotEmpty(),
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            state.failingIndexerNames.forEach { name ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        Modifier.size(4.dp).clip(CircleShape).background(ArrRed)
                                    )
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

//@Composable
//private fun IndexerStatItem(
//    modifier: Modifier = Modifier,
//    icon: ImageVector,
//    label: String,
//    count: Int,
//    color: Color
//) {
//    Box(
//        modifier = modifier
//            .clip(RoundedCornerShape(12.dp))
//            .background(color)
//            .padding(vertical = 8.dp, horizontal = 12.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = label,
//            style = MaterialTheme.typography.labelLarge,
//            fontWeight = FontWeight.Bold,
//            color = textColor
//        )
//    }
//}

@Composable
private fun SeerrSection(seerrInstances: List<SeerrDashboardState>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        seerrInstances.forEach { state ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = state.instance.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CountStatItem(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ConfirmationNumber,
                            label = mokoString(MR.strings.requests),
                            count = state.pendingRequestsCount,
                            color = ArrPurple
                        )
                        CountStatItem(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.BugReport,
                            label = mokoString(MR.strings.issues),
                            count = state.openIssuesCount,
                            color = when {
                                state.openIssuesCount > 0 -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountStatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    count: Int,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, null, modifier = Modifier.size(24.dp))
                Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun DownloadClientsSection(
    state: CombinedDashboardState.Success
) {
    val clients = state.downloadClients
    val totalDownloadSpeed = state.downloadTransfers.sumOf { it.downloadSpeed }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            clients.forEach { state ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(state.client.type.icon),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.client.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "${state.activeDownloadsCount} ${mokoString(MR.strings.downloads)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!state.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error)
                                )
                                Text(
                                    text = mokoString(MR.strings.offline),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    val transferInfo = state.transferInfo
                    if (state.isOnline && (transferInfo != null)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    Icons.Default.ExpandMore, null,
                                    tint = ArrGreen, modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${transferInfo.downloadSpeed.bytesAsFileSizeString()}/s",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    Icons.Default.ExpandLess, null,
                                    tint = ArrBlue, modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${transferInfo.uploadSpeed.bytesAsFileSizeString()}/s",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (state.activeDownloads.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Download, null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        mokoString(MR.strings.downloads),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    if (totalDownloadSpeed > 0) {
                        Text(
                            "${totalDownloadSpeed.bytesAsFileSizeString()}/s",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                state.activeDownloads.take(5).forEach { download ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                download.name,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${(download.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${download.downloaded.bytesAsFileSizeString()} / ${download.size.bytesAsFileSizeString()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.weight(1f))
                            if (download.downloadSpeed > 0) {
                                Text(
                                    text = "${download.downloadSpeed.bytesAsFileSizeString()}/s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ArrGreen
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { download.progress.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        )
                    }
                }

                if (state.activeDownloads.size > 5) {
                    Text(
                        mokoString(
                            MR.strings.additional_items_count,
                            state.activeDownloads.size - 5
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentActivitySection(activity: List<QueueItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.History, null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = mokoString(MR.strings.activity),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            activity.take(5).forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (item.hasIssue) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            item.title ?: item.titleLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            item.instanceName?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                item.statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.hasIssue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (item.sizeleft > 0) {
                        Text(
                            item.progressLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (activity.size > 5) {
                Text(
                    mokoString(
                        MR.strings.additional_items_count,
                        activity.size - 5
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
