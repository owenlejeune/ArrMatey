package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.entensions.rememberHtml
import com.dnfapps.arrmatey.entensions.unlessEmpty
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.helpers.LocalFloatingBarBottomPadding
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.ArrPurple
import com.dnfapps.arrmatey.ui.theme.TranslucentBlack
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.Blur
import com.dnfapps.arrmatey.utils.MultiSelectState
import com.dnfapps.arrmatey.utils.PosterElevation
import com.dnfapps.arrmatey.utils.PosterRadius
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun <T : ArrMedia> MediaList(
    aspectRatio: AspectRatio,
    items: List<T>,
    onItemClick: (T) -> Unit,
    itemIsActive: (T) -> Boolean,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    showBannerBackground: Boolean = true,
    includeOverview: Boolean = false,
    blur: Blur = Blur.Normal,
    posterElevation: PosterElevation = PosterElevation.Medium,
    posterRadius: PosterRadius = PosterRadius.Medium,
    multiSelectState: MultiSelectState<Long> = MultiSelectState(selectionModeAvailable = false),
    qualityProfiles: List<QualityProfile> = emptyList(),
    tags: List<Tag> = emptyList(),
) {
    val bottomPadding = LocalFloatingBarBottomPadding.current

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = userScrollEnabled,
        contentPadding =
            PaddingValues(
                start = 12.dp,
                top = 12.dp,
                end = 12.dp,
                bottom = 12.dp + bottomPadding,
            ),
    ) {
        items(
            items = items,
            key = { item -> item.guid },
        ) { item ->
            val isActive = itemIsActive(item)
            MediaItem(
                aspectRatio = aspectRatio,
                item = item,
                onItemClick = onItemClick,
                isActive = isActive,
                showBannerBackground = showBannerBackground,
                includeOverview = includeOverview,
                blur = blur,
                posterElevation = posterElevation,
                posterRadius = posterRadius,
                multiSelectState = multiSelectState,
                qualityProfiles = qualityProfiles,
                tags = tags,
            )
        }
    }
}

@Composable
fun SearchResultList(
    items: List<SearchResult>,
    onItemClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    includeOverview: Boolean = true,
    showBanners: Boolean = true,
) {
    var wasAtTop by remember { mutableStateOf(true) }

    LaunchedEffect(lazyListState) {
        snapshotFlow { !lazyListState.canScrollBackward }
            .collect { atTop ->
                wasAtTop = atTop
            }
    }

    LaunchedEffect(items) {
        if (wasAtTop && items.isNotEmpty()) {
            lazyListState.scrollToItem(0)
        }
    }

    val bottomPadding = LocalFloatingBarBottomPadding.current
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding =
            PaddingValues(
                top = 12.dp,
                bottom = bottomPadding + 16.dp,
                start = 18.dp,
                end = 18.dp,
            ),
    ) {
        items(items, key = { it.id }) { item ->
            SearchResultItem(
                item = item,
                onItemClick = onItemClick,
                includeOverview = includeOverview,
                showBanners = showBanners,
            )
        }
    }
}

@Composable
fun SearchResultItem(
    item: SearchResult,
    onItemClick: (SearchResult) -> Unit,
    includeOverview: Boolean = true,
    showBanners: Boolean = true,
) {
    val edgeColor =
        remember(item) {
            item.instanceType.associatedColor
        }

    when (item) {
        is SearchResult.ArrMediaResult -> {
            MediaItem(
                aspectRatio = item.aspectRatio,
                item = item.media,
                onItemClick = { onItemClick(item) },
                includeOverview = includeOverview,
                showBannerBackground = showBanners,
                edgeColor = edgeColor,
            )
        }

        is SearchResult.SeerrMediaResult -> {
            SeerrMediaItem(
                result = item,
                onItemClick = onItemClick,
                includeOverview = includeOverview,
                showBannerBackground = showBanners,
                edgeColor = edgeColor,
            )
        }

        is SearchResult.SeerrPersonResult -> {
            SeerrPersonItem(
                result = item,
                onItemClick = onItemClick,
                includeOverview = includeOverview,
                edgeColor = edgeColor,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : ArrMedia> MediaItem(
    aspectRatio: AspectRatio,
    item: T,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    showBannerBackground: Boolean = true,
    includeOverview: Boolean = false,
    posterModel: Any? = null,
    bannerModel: Any? = null,
    blur: Blur = Blur.Normal,
    posterElevation: PosterElevation = PosterElevation.Medium,
    posterRadius: PosterRadius = PosterRadius.Medium,
    multiSelectState: MultiSelectState<Long> = MultiSelectState(selectionModeAvailable = false),
    edgeColor: Color? = null,
    qualityProfiles: List<QualityProfile> = emptyList(),
    tags: List<Tag> = emptyList(),
) {
    val isSelected = multiSelectState.isSelected(item.guid)
    val isInSelectionMode by multiSelectState.isInSelectionMode.collectAsStateWithLifecycle()
    val isSelectionModeAvailable by multiSelectState.isSelectionModeAvailable.collectAsStateWithLifecycle()

    var contentHeight by remember { mutableIntStateOf(0) }

    val hasBanner =
        remember(showBannerBackground, bannerModel, item) {
            showBannerBackground && (bannerModel != null || item.getBanner()?.remoteUrl != null)
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (isInSelectionMode) {
                            multiSelectState.toggle(item.guid)
                        } else {
                            onItemClick(item)
                        }
                    },
                    onLongClick = {
                        if (isSelectionModeAvailable) {
                            multiSelectState.toggle(item.guid)
                        }
                    },
                ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = posterElevation.elevation),
        border =
            if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            },
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        ) {
            if (edgeColor != null) {
                Box(
                    modifier =
                        Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(edgeColor),
                )
            }

            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                if (hasBanner) {
                    BannerView(
                        bannerModel = bannerModel ?: item.getBanner()?.remoteUrl?.let { rememberRemoteImageData(it) },
                        blur = blur,
                        modifier = Modifier.matchParentSize(),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PosterItem(
                        item = item,
                        aspectRatio = aspectRatio,
                        modifier = Modifier.width(76.dp),
                        posterModel = posterModel,
                        elevation = posterElevation,
                        radius = posterRadius,
                        multiSelectState = multiSelectState,
                        additionalContent = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shadowElevation = 2.dp,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopStart)
                                        .padding(4.dp),
                            ) {
                                Box(
                                    modifier = Modifier.padding(3.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector =
                                            if (item.monitored) {
                                                Icons.Default.Bookmark
                                            } else {
                                                Icons.Default.BookmarkBorder
                                            },
                                        contentDescription =
                                            mokoString(
                                                if (item.monitored) MR.strings.monitored else MR.strings.unmonitored,
                                            ),
                                        tint =
                                            if (item.monitored) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                        modifier = Modifier.size(12.dp),
                                    )
                                }
                            }
                        },
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val titleColor = if (hasBanner) Color.White else MaterialTheme.colorScheme.onSurface
                        Text(
                            text = item.title ?: mokoString(MR.strings.unknown),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        MediaDetails(
                            item = item,
                            isActive = isActive,
                            showBannerBackground = hasBanner,
                            qualityProfiles = qualityProfiles,
                            tags = tags,
                        )

                        if (includeOverview && item.overview != null) {
                            val parsed = item.overview?.rememberHtml() ?: ""
                            Text(
                                text = parsed,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasBanner) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeerrMediaItem(
    result: SearchResult.SeerrMediaResult,
    onItemClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
    includeOverview: Boolean = true,
    showBannerBackground: Boolean = true,
    bannerModel: Any? = null,
    edgeColor: Color? = null,
) {
    val item = result.result
    val hasBanner =
        remember(showBannerBackground, bannerModel, item) {
            showBannerBackground && (bannerModel != null || item.fullBackdropPath != null)
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(onClick = { onItemClick(result) }),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        ) {
            if (edgeColor != null) {
                Box(
                    modifier =
                        Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(edgeColor),
                )
            }

            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                if (hasBanner) {
                    BannerView(
                        bannerModel = bannerModel ?: item.fullBackdropPath?.let { rememberRemoteImageData(it) },
                        blur = Blur.Normal,
                        modifier = Modifier.matchParentSize(),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PosterItem(
                        item = item,
                        modifier = Modifier.width(76.dp),
                        showFooter = false,
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val titleColor = if (hasBanner) Color.White else MaterialTheme.colorScheme.onSurface
                        Text(
                            text = item.title ?: item.name ?: mokoString(MR.strings.unknown),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        val releaseDate = item.releaseDate ?: item.firstAirDate
                        val year = releaseDate?.take(4)
                        val secondLine = listOfNotNull(year, item.mediaType.name).joinToString(BULLET)
                        Text(
                            text = secondLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (hasBanner) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (includeOverview && item.overview != null) {
                            Text(
                                text = item.overview ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasBanner) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeerrPersonItem(
    result: SearchResult.SeerrPersonResult,
    onItemClick: (SearchResult) -> Unit,
    includeOverview: Boolean = true,
    edgeColor: Color? = null,
) {
    val item = result.result
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .combinedClickable(onClick = { onItemClick(result) }),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        ) {
            if (edgeColor != null) {
                Box(
                    modifier =
                        Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(edgeColor),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.Top,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(12.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
            ) {
                PersonProfileImage(item.fullPosterPath)

                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                    verticalArrangement = Arrangement.Top,
                ) {
                    Text(
                        text = item.name ?: mokoString(MR.strings.unknown),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (includeOverview && item.knownFor.isNotEmpty()) {
                        val knownFor = item.knownFor.joinToString(", ") { it.title ?: it.name ?: "" }
                        Text(
                            text = knownFor,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaDetails(
    item: ArrMedia,
    isActive: Boolean,
    showBannerBackground: Boolean,
    qualityProfiles: List<QualityProfile> = emptyList(),
    tags: List<Tag> = emptyList(),
) {
    Column {
        when (item) {
            is ArrSeries -> SeriesDetails(item, isActive, showBannerBackground, qualityProfiles, tags)
            is ArrMovie -> MovieDetails(item, isActive, showBannerBackground, qualityProfiles, tags)
            is Arrtist -> ArtistDetails(item, isActive, showBannerBackground, qualityProfiles, tags)
            is Author -> AuthorDetails(item, isActive, showBannerBackground, qualityProfiles, tags)
            is Audiobook -> AudiobookDetails(item, isActive, showBannerBackground)
            is SearchAudiobook -> SearchAudiobookDetails(item, showBannerBackground)
            is MockMedia -> MockDetails(item, showBannerBackground)
        }
    }
}

@Composable
private fun SeriesDetails(
    item: ArrSeries,
    isActive: Boolean,
    showBannerBackground: Boolean,
    qualityProfiles: List<QualityProfile> = emptyList(),
    tags: List<Tag> = emptyList(),
) {
    val contentColor = if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryContentColor = if (showBannerBackground) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
    val seasonLabel = mokoPlural(MR.plurals.seasons, item.seasonCount)
    val fileSizeString = item.fileSize?.bytesAsFileSizeString()?.takeUnless { item.id == null }
    val network = item.network

    val secondLine = listOfNotNull(seasonLabel, fileSizeString, network).joinToString(BULLET)
    Text(secondLine, color = contentColor, style = MaterialTheme.typography.bodyMedium)

    val nextAirStr = item.nextAiring?.format()
    val statusStr =
        when (item.status) {
            MediaStatus.Continuing ->
                nextAirStr
                    ?: "${mokoString(item.status.resource)} - ${mokoString(MR.strings.unknown)}"

            else -> listOfNotNull(mokoString(item.status.resource), nextAirStr).joinToString(BULLET)
        }
    Text(statusStr, color = contentColor, style = MaterialTheme.typography.bodyMedium)

    val qualityProfile = qualityProfiles.firstOrNull { it.id == item.qualityProfileId }?.name ?: item.profileName
    val tagsLabel = item.formatTags(tags)
    val metaLine = listOfNotNull(qualityProfile, tagsLabel).joinToString(BULLET)
    if (metaLine.isNotEmpty()) {
        Text(metaLine, color = secondaryContentColor, style = MaterialTheme.typography.bodySmall)
    }

    if (item.id != null) {
        Text(
            text = "${item.episodeFileCount}/${item.episodeCount}",
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
        )
        LinearProgressIndicator(
            progress = { item.statusProgress },
            color = if (isActive) ArrPurple else item.statusColor,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun MovieDetails(
    item: ArrMovie,
    isActive: Boolean,
    showBannerBackground: Boolean,
    qualityProfiles: List<QualityProfile> = emptyList(),
    tags: List<Tag> = emptyList(),
) {
    val contentColor = if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryContentColor = if (showBannerBackground) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    val nextReleaseDate =
        item.digitalRelease?.format("MMM d, yyyy")?.let { "${mokoString(MR.strings.digital_release)}: $it" }
            ?: item.physicalRelease?.format("MMM d, yyyy")?.let { "${mokoString(MR.strings.physical_release)}: $it" }
            ?: item.inCinemas?.format("MMM d, yyyy")?.let { "${mokoString(MR.strings.in_cinemas)}: $it" }
            ?: item.releaseDate?.format("MMMM d, yyyy")

    nextReleaseDate?.let {
        Text(it, color = contentColor, style = MaterialTheme.typography.bodyMedium)
    }

    val firstLine = listOfNotNull(item.runtimeString, item.studio).joinToString(" • ")
    firstLine.unlessEmpty { firstLine ->
        Text(firstLine, color = contentColor, style = MaterialTheme.typography.bodyMedium)
    }

    val qualityLabel =
        item.movieFile
            ?.quality
            ?.quality
            ?.name
    val fileSizeLabel = item.fileSize?.bytesAsFileSizeString()?.takeUnless { item.id == null }
    val thirdLine = listOfNotNull(qualityLabel, fileSizeLabel).joinToString(BULLET)
    thirdLine.unlessEmpty { thirdLine ->
        Text(thirdLine, color = contentColor, style = MaterialTheme.typography.bodyMedium)
    }

    val qualityProfile = qualityProfiles.firstOrNull { it.id == item.qualityProfileId }?.name
    val tagsLabel = item.formatTags(tags)
    val metaLine = listOfNotNull(qualityProfile, tagsLabel).joinToString(BULLET)
    if (metaLine.isNotEmpty()) {
        Text(metaLine, color = secondaryContentColor, style = MaterialTheme.typography.bodySmall)
    }

    if (item.id != null) {
        LinearProgressIndicator(
            progress = { item.statusProgress },
            color = if (isActive) ArrPurple else item.statusColor,
            modifier =
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun ArtistDetails(
    item: Arrtist,
    isActive: Boolean,
    showBannerBackground: Boolean,
    qualityProfiles: List<QualityProfile> = emptyList(),
    tags: List<Tag> = emptyList(),
) {
    val contentColor = if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryContentColor = if (showBannerBackground) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
    val albumLabel = mokoPlural(MR.plurals.albums, item.albumCount)
    val trackLabel = mokoPlural(MR.plurals.tracks, item.trackCount)
    val secondLine = listOfNotNull(albumLabel, trackLabel).joinToString(BULLET)
    Text(secondLine, color = contentColor, style = MaterialTheme.typography.bodyMedium)

    val nextRelease = item.nextAlbum?.releaseDate?.format()
    val statusStr =
        if (nextRelease != null) {
            "${mokoString(item.status.resource)} • $nextRelease"
        } else {
            mokoString(item.status.resource)
        }
    Text(statusStr, color = contentColor, style = MaterialTheme.typography.bodyMedium)

    val qualityProfile = qualityProfiles.firstOrNull { it.id == item.qualityProfileId }?.name
    val tagsLabel = item.formatTags(tags)
    val metaLine = listOfNotNull(qualityProfile, tagsLabel).joinToString(BULLET)
    if (metaLine.isNotEmpty()) {
        Text(metaLine, color = secondaryContentColor, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AuthorDetails(
    item: Author,
    isActive: Boolean,
    showBannerBackground: Boolean,
    qualityProfiles: List<QualityProfile> = emptyList(),
    tags: List<Tag> = emptyList(),
) {
    val contentColor = if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryContentColor = if (showBannerBackground) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    val bookLabel = mokoPlural(MR.plurals.books_count, item.bookCount)
    val firstLine = listOfNotNull(bookLabel).joinToString(BULLET)
    Text(firstLine, color = contentColor, style = MaterialTheme.typography.bodyMedium)

    val nextRelease = item.nextBook?.releaseDate?.format()
    val statusStr =
        when (item.status) {
            MediaStatus.Continuing ->
                nextRelease
                    ?: "${mokoString(item.status.resource)} - ${mokoString(MR.strings.unknown)}"

            else -> listOfNotNull(mokoString(item.status.resource), nextRelease).joinToString(BULLET)
        }
    Text(statusStr, color = contentColor, style = MaterialTheme.typography.bodyMedium)

    val qualityProfile = qualityProfiles.firstOrNull { it.id == item.qualityProfileId }?.name
    val tagsLabel = item.formatTags(tags)
    val metaLine = listOfNotNull(qualityProfile, tagsLabel).joinToString(BULLET)
    if (metaLine.isNotEmpty()) {
        Text(metaLine, color = secondaryContentColor, style = MaterialTheme.typography.bodySmall)
    }

    if (item.id != null) {
        Text(
            text = "${item.bookFileCount}/${item.bookCount}",
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
        )
        LinearProgressIndicator(
            progress = { item.statusProgress },
            color = if (isActive) ArrPurple else item.statusColor,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun AudiobookDetails(
    item: Audiobook,
    isActive: Boolean,
    showBannerBackground: Boolean,
) {
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface

    val authorString = item.authors.joinToString(", ")
    Text(authorString, color = contentColor, style = MaterialTheme.typography.bodyMedium)

    val seriesString =
        item.series?.let {
            if (item.seriesNumber != null) "$it (#${item.seriesNumber})" else it
        }
    val fileSizeString = item.fileSize?.bytesAsFileSizeString()?.takeUnless { item.id == null }

    val secondLine = listOfNotNull(seriesString, fileSizeString, item.publisher).joinToString(BULLET)
    if (secondLine.isNotEmpty()) {
        Text(secondLine, color = contentColor, style = MaterialTheme.typography.bodyMedium)
    }

    val releaseDate = item.publishedDate?.format("MMMM d, yyyy") ?: item.publishYear
    val statusStr = listOfNotNull(mokoString(item.status.resource), releaseDate).joinToString(BULLET)
    Text(statusStr, color = contentColor, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun SearchAudiobookDetails(
    item: SearchAudiobook,
    showBannerBackground: Boolean,
) {
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface

    val authorString = item.authors.joinToString(", ") { it.name }
    Text(authorString, color = contentColor, style = MaterialTheme.typography.bodyMedium)

    val seriesString =
        item.series.firstOrNull()?.let {
            if (it.position != null) "${it.name} (#${it.position})" else it.name
        }
    val secondLine = listOfNotNull(seriesString, item.publisher).joinToString(BULLET)
    if (secondLine.isNotEmpty()) {
        Text(secondLine, color = contentColor, style = MaterialTheme.typography.bodyMedium)
    }

    item.releaseDate?.format("MMMM d, yyyy")?.let {
        Text(it, color = contentColor, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MockDetails(
    item: MockMedia,
    showBannerBackground: Boolean,
) {
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryContentColor =
        if (showBannerBackground) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    Text("Mock Studio", color = contentColor, style = MaterialTheme.typography.bodyMedium)
    Text("Mock Quality", color = contentColor, style = MaterialTheme.typography.bodyMedium)
    Text("Any • 1080p • HD", color = secondaryContentColor, style = MaterialTheme.typography.bodySmall)
}

@Composable
fun BannerView(
    bannerModel: Any?,
    modifier: Modifier = Modifier,
    blur: Blur = Blur.Normal,
) {
    val blurModifier =
        if (blur.radius > 0) {
            Modifier.blur(blur.radius.dp)
        } else {
            Modifier
        }

    Box(modifier = modifier) {
        when (bannerModel) {
            is Painter -> {
                Image(
                    painter = bannerModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().then(blurModifier),
                    contentScale = ContentScale.Crop,
                    alpha = 0.5f,
                )
            }

            else -> {
                AsyncImage(
                    model = bannerModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().then(blurModifier),
                    contentScale = ContentScale.Crop,
                    alpha = 0.5f,
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(TranslucentBlack),
        )
    }
}
