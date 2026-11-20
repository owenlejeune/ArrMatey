package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.newest.NewestDownloadableItem
import com.dnfapps.arrmatey.newest.NewestDownloadableState
import com.dnfapps.arrmatey.newest.ReleaseKind
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.StringResource

/**
 * A Discover row of the newest releases from Prowlarr. Titles Seerr matched show as
 * posters; the rest show as plain cards with their year, quality and seeders.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewestDownloadableRow(
    title: StringResource,
    icon: ImageVector,
    state: NewestDownloadableState,
    onItemClick: (NewestDownloadableItem) -> Unit,
    onSeeMore: (() -> Unit)? = null,
) {
    val items = (state as? NewestDownloadableState.Success)?.items.orEmpty()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = mokoString(title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (onSeeMore != null) {
                TextButton(onClick = onSeeMore) {
                    Text(
                        text = mokoString(MR.strings.see_more),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        when {
            state is NewestDownloadableState.Error -> {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            state !is NewestDownloadableState.Success -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }
            items.isEmpty() -> {
                Text(
                    text = mokoString(MR.strings.newest_downloadable_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            else -> NewestPosterRow(items = items, onItemClick = onItemClick)
        }
    }
}

@Composable
private fun NewestPosterRow(
    items: List<NewestDownloadableItem>,
    onItemClick: (NewestDownloadableItem) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = items,
            key = { it.key },
            contentType = { if (it.match != null) "poster" else "plain" },
        ) { item ->
            val match = item.match
            if (match != null) {
                PosterItem(
                    item = match,
                    modifier = Modifier.width(120.dp),
                    onItemClick = { onItemClick(item) },
                )
            } else {
                PlainReleaseCard(item = item)
            }
        }
    }
}

@Composable
fun PlainReleaseCard(
    item: NewestDownloadableItem,
    modifier: Modifier = Modifier.width(160.dp).height(180.dp),
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = item.displayTitle,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            item.displayYear?.let {
                Text(text = it.toString(), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = newestDetails(item),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The Prowlarr tab's Movies and TV sub-tabs: a list of the newest releases. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewestDownloadableList(
    state: NewestDownloadableState,
    onItemClick: (NewestDownloadableItem) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isRefreshing by remember { mutableStateOf(false) }
    if (state !is NewestDownloadableState.Loading) isRefreshing = false

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (state) {
            NewestDownloadableState.Initial, NewestDownloadableState.Loading -> {
                LoadingIndicator(modifier = Modifier.size(96.dp))
            }
            is NewestDownloadableState.Error -> {
                Text(text = state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            is NewestDownloadableState.Success -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        onRefresh()
                    },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    if (state.items.isEmpty()) {
                        Text(
                            text = mokoString(MR.strings.newest_downloadable_empty),
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items = state.items, key = { it.key }) { item ->
                                NewestDownloadableListItem(item = item, onClick = { onItemClick(item) })
                            }
                        }
                    }
                }
            }
        }
    }
}

/** A list row: poster, title, "2026 • Movie • 2160p • 45 seeds", and optionally the overview. */
@Composable
fun NewestDownloadableListItem(
    item: NewestDownloadableItem,
    onClick: () -> Unit,
    showOverview: Boolean = false,
) {
    val poster = item.match?.fullPosterPath
    val overview = item.match?.overview?.takeIf { showOverview && it.isNotBlank() }
    ListItem(
        modifier = if (item.match != null) Modifier.clickable(onClick = onClick) else Modifier,
        leadingContent = poster?.let {
            {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                    Modifier
                        .width(48.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
            }
        },
        headlineContent = {
            Text(text = item.displayTitle, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Column {
                Text(text = newestListLine(item))
                if (overview != null) {
                    Text(text = overview, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
        },
    )
}

@Composable
private fun newestListLine(item: NewestDownloadableItem): String {
    val type = mokoString(if (item.release.kind == ReleaseKind.Movie) MR.strings.movie else MR.strings.series)
    return listOfNotNull(item.displayYear?.toString(), type, newestDetails(item)).joinToString(" • ")
}

@Composable
private fun newestDetails(item: NewestDownloadableItem): String {
    val release = item.release
    val season = release.parsed.season?.takeIf { release.kind == ReleaseKind.Tv }?.let { "S" + it.toString().padStart(2, '0') }
    val seeders = release.seeders?.let { mokoString(MR.strings.seeders_count, it) }
    val age = release.ageHours.let { if (it < 24) "${it.toInt()}h" else "${(it / 24).toInt()}d" }
    return listOfNotNull(season, release.qualities.joinToString("/").ifEmpty { null }, seeders, age).joinToString(" • ")
}
