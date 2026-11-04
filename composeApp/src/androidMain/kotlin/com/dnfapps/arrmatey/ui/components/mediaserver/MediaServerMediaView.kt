package com.dnfapps.arrmatey.ui.components.mediaserver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.utils.isTitleSort
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.mediaserver.model.UnifiedMediaItem
import com.dnfapps.arrmatey.navigation.navigationManager
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AlphabetFastScroller
import com.dnfapps.arrmatey.ui.components.BannerView
import com.dnfapps.arrmatey.ui.components.BasePosterItem
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.helpers.MinColumnsAdaptiveGridCells
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.FastScrollUtils
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.coroutines.launch

@Composable
fun MediaServerMediaView(
    items: List<UnifiedMediaItem>,
    preferences: InstancePreferences,
    onItemClick: (UnifiedMediaItem) -> Unit,
    modifier: Modifier = Modifier,
    onToggleWatched: ((UnifiedMediaItem) -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val bottomPadding = LocalFloatingBarBottomPadding.current

    val isSortByTitle = preferences.sortBy.isTitleSort
    val alphabet = remember(preferences.sortOrder) {
        FastScrollUtils.getAlphabet(preferences.sortOrder)
    }
    val letterIndexMap = remember(items, preferences.sortBy) {
        FastScrollUtils.buildLetterIndexMap(items) { it.title }
    }

    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp),
                )
                Text(
                    text = mokoString(MR.strings.no_results_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    val contentModifier = Modifier
        .fillMaxSize()
        .padding(end = if (isSortByTitle && items.isNotEmpty()) 24.dp else 0.dp)

    Box(modifier = modifier.fillMaxSize()) {
        when (preferences.viewType) {
            ViewType.Grid -> {
                LazyVerticalGrid(
                    state = gridState,
                    columns = MinColumnsAdaptiveGridCells(minSize = preferences.gridDensity.minSize, minColumns = 3),
                    contentPadding = PaddingValues(
                        start = preferences.gridSpacing.spacing,
                        top = preferences.gridSpacing.spacing,
                        end = preferences.gridSpacing.spacing,
                        bottom = preferences.gridSpacing.spacing + bottomPadding,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(preferences.gridSpacing.spacing),
                    verticalArrangement = Arrangement.spacedBy(preferences.gridSpacing.spacing),
                    modifier = contentModifier,
                ) {
                    items(items, key = { it.id }) { item ->
                        MediaServerPosterGridItem(
                            item = item,
                            preferences = preferences,
                            onClick = { onItemClick(item) },
                            onToggleWatched = onToggleWatched?.let { { it(item) } },
                        )
                    }
                }
            }

            ViewType.List -> {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        top = 12.dp,
                        end = 12.dp,
                        bottom = 12.dp + bottomPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = contentModifier,
                ) {
                    items(items, key = { it.id }) { item ->
                        MediaServerListItem(
                            item = item,
                            preferences = preferences,
                            onClick = { onItemClick(item) },
                            onToggleWatched = onToggleWatched?.let { { it(item) } },
                        )
                    }
                }
            }
        }

        if (isSortByTitle && items.isNotEmpty()) {
            AlphabetFastScroller(
                alphabet = alphabet,
                onLetterSelected = { selectedLetter ->
                    val targetIndex = FastScrollUtils.findTargetIndex(
                        selectedLetter = selectedLetter,
                        letterIndexMap = letterIndexMap,
                        alphabet = alphabet,
                        itemCount = items.size,
                    )
                    coroutineScope.launch {
                        when (preferences.viewType) {
                            ViewType.List -> listState.scrollToItem(targetIndex)
                            ViewType.Grid -> gridState.scrollToItem(targetIndex)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
fun MediaServerMediaView(
    items: List<UnifiedMediaItem>,
    viewType: ViewType,
    onItemClick: (UnifiedMediaItem) -> Unit,
    modifier: Modifier = Modifier,
    onToggleWatched: ((UnifiedMediaItem) -> Unit)? = null,
) {
    MediaServerMediaView(
        items = items,
        preferences = InstancePreferences(viewType = viewType),
        onItemClick = onItemClick,
        modifier = modifier,
        onToggleWatched = onToggleWatched,
    )
}

@Composable
fun MediaServerPosterGridItem(
    item: UnifiedMediaItem,
    preferences: InstancePreferences,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleWatched: (() -> Unit)? = null,
) {
    BasePosterItem(
        model = item.posterUrl ?: "",
        modifier = modifier,
        elevation = preferences.posterElevation,
        radius = preferences.posterRadius,
        aspectRatio = AspectRatio.Poster,
        onClick = onClick,
        onLongClick = onToggleWatched,
        additionalContent = {
            if (preferences.showOverlay) {
                // Watched badge
                if (item.played) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                    ) {
                        Box(
                            modifier = Modifier.padding(4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = mokoString(MR.strings.watched),
                                tint = Color(0xFF50D27D),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                // Resolution badge
                item.videoResolution?.let { res ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                    ) {
                        Text(
                            text = res.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        )
                    }
                }

                // Progress indicator
                val progress = item.progressPercentage
                if (progress != null && progress > 0f && !item.played) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        },
        footerVisible = preferences.showFullDetails,
        footerContent = {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = when {
                item.seriesTitle != null && item.seasonNumber != null && item.episodeNumber != null ->
                    "S${item.seasonNumber}:E${item.episodeNumber}"
                item.seriesTitle != null -> item.seriesTitle
                item.year != null -> item.year.toString()
                else -> null
            }
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaServerListItem(
    item: UnifiedMediaItem,
    preferences: InstancePreferences,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleWatched: (() -> Unit)? = null,
) {
    val hasBanner = preferences.showBannerBackground && !item.backdropUrl.isNullOrBlank()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onToggleWatched,
            ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = preferences.posterElevation.elevation),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            // Edge brand color
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(item.serverType.brandColor),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                if (hasBanner) {
                    BannerView(
                        bannerModel = item.backdropUrl,
                        blur = preferences.bannerBlur,
                        modifier = Modifier.matchParentSize(),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // Thumbnail poster
                    BasePosterItem(
                        model = item.posterUrl ?: "",
                        aspectRatio = AspectRatio.Poster,
                        modifier = Modifier.width(76.dp),
                        elevation = preferences.posterElevation,
                        radius = preferences.posterRadius,
                        additionalContent = {
                            if (preferences.showOverlay) {
                                if (item.played) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                        shadowElevation = 2.dp,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(3.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = mokoString(MR.strings.watched),
                                                tint = Color(0xFF50D27D),
                                                modifier = Modifier.size(12.dp),
                                            )
                                        }
                                    }
                                }

                                val progress = item.progressPercentage
                                if (progress != null && progress > 0f && !item.played) {
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 4.dp)
                                            .height(3.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        },
                    )

                    // Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val titleColor = if (hasBanner) Color.White else MaterialTheme.colorScheme.onSurface
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        val subtitle = when {
                            item.seriesTitle != null && item.seasonNumber != null && item.episodeNumber != null ->
                                "${item.seriesTitle} • S${item.seasonNumber}:E${item.episodeNumber}"
                            item.seriesTitle != null -> item.seriesTitle
                            item.year != null -> item.year.toString()
                            else -> null
                        }
                        val subtitleColor = if (hasBanner) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = subtitleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        // Tags row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            item.videoResolution?.let { res ->
                                Text(
                                    text = res.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                            item.audioCodec?.let { codec ->
                                Text(
                                    text = codec.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (hasBanner) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            item.year?.takeIf { item.seriesTitle != null }?.let { yr ->
                                Text(
                                    text = yr.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (hasBanner) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        // Overview
                        val summary = item.summary
                        if (preferences.includeOverview && !summary.isNullOrBlank()) {
                            Text(
                                text = summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasBanner) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    // Trailing status
                    if (item.played) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = mokoString(MR.strings.watched),
                            tint = Color(0xFF50D27D),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaServerEmptyConfigView(
    modifier: Modifier = Modifier,
) {
    val nav = navigationManager
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
            }

            Text(
                text = mokoString(MR.strings.no_media_servers_configured),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = mokoString(MR.strings.no_media_servers_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
                onClick = { nav.openIntegrationsSettings() },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(mokoString(MR.strings.integrations))
            }
        }
    }
}
