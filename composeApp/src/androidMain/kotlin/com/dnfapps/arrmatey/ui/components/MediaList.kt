package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.discover.model.SearchResult
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.entensions.rememberHtml
import com.dnfapps.arrmatey.entensions.unlessEmpty
import com.dnfapps.arrmatey.extensions.pxToDp
import com.dnfapps.arrmatey.shared.MR
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
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        userScrollEnabled = userScrollEnabled,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 18.dp),
    ) {
        items(items) { item ->
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
            )
        }
    }
}

@Composable
fun SearchResultList(
    items: List<SearchResult>,
    onItemClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
    includeOverview: Boolean = true,
    showBanners: Boolean = true,
    showInstanceIndicatorShadow: Boolean = true,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 18.dp),
    ) {
        items(items, key = { it.id }) { item ->
            SearchResultItem(
                item = item,
                onItemClick = onItemClick,
                includeOverview = includeOverview,
                showBanners = showBanners,
                showInstanceIndicatorShadow = showInstanceIndicatorShadow,
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
    showInstanceIndicatorShadow: Boolean = true,
) {
    val shadowColor =
        remember(item, showInstanceIndicatorShadow) {
            if (showInstanceIndicatorShadow) item.instanceType.associatedColor else Color.Unspecified
        }

    Box(
        modifier =
            Modifier // .colouredDropShadow(shadowColor)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(10.dp),
                    ambientColor = shadowColor,
                    spotColor = shadowColor,
                ),
    ) {
        when (item) {
            is SearchResult.ArrMediaResult -> {
                MediaItem(
                    aspectRatio = item.aspectRatio,
                    item = item.media,
                    onItemClick = { onItemClick(item) },
                    includeOverview = includeOverview,
                    showBannerBackground = showBanners,
                )
            }
            is SearchResult.SeerrMediaResult -> {
                SeerrMediaItem(
                    result = item,
                    onItemClick = onItemClick,
                    includeOverview = includeOverview,
                    showBannerBackground = showBanners,
                )
            }
            is SearchResult.SeerrPersonResult -> {
                SeerrPersonItem(
                    result = item,
                    onItemClick = onItemClick,
                    includeOverview = includeOverview,
                )
            }
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
) {
    val isSelected = multiSelectState.isSelected(item.guid)
    val isInSelectionMode by multiSelectState.isInSelectionMode.collectAsStateWithLifecycle()
    val isSelectionModeAvailable by multiSelectState.isSelectionModeAvailable.collectAsStateWithLifecycle()

    var contentHeight by remember { mutableIntStateOf(0) }

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
        shape = RoundedCornerShape(posterRadius.radius),
        elevation = CardDefaults.cardElevation(defaultElevation = posterElevation.elevation),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
        ) {
            if (showBannerBackground && (bannerModel != null || item.getBanner()?.remoteUrl != null)) {
                BannerView(
                    bannerModel = bannerModel ?: item.getBanner()?.remoteUrl?.let { rememberRemoteImageData(it) },
                    blur = blur,
                    modifier = Modifier.height(contentHeight.pxToDp() + 24.dp),
                )
            }

            Row(
                modifier =
                    Modifier
                        .wrapContentHeight()
                        .padding(12.dp)
                        .onGloballyPositioned {
                            contentHeight = it.size.height
                        },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PosterItem(
                    item = item,
                    aspectRatio = aspectRatio,
                    modifier = Modifier.width(75.dp),
                    posterModel = posterModel,
                    elevation = posterElevation,
                    radius = posterRadius,
                    multiSelectState = multiSelectState,
                )

                Column(
                    modifier = Modifier.weight(1f).wrapContentHeight(),
                    verticalArrangement = Arrangement.Top,
                ) {
                    val titleColor = if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
                    Text(
                        text = item.title ?: mokoString(MR.strings.unknown),
                        style = MaterialTheme.typography.titleLarge,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    MediaDetails(item, isActive, showBannerBackground)

                    if (includeOverview && item.overview != null) {
                        val parsed = item.overview?.rememberHtml() ?: ""
                        Text(
                            text = parsed,
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                if (showBannerBackground) {
                                    Color.White.copy(
                                        alpha = 0.8f,
                                    )
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            maxLines = 6,
                            minLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 8.dp),
                        )
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
) {
    var contentHeight by remember { mutableIntStateOf(0) }

    val item = result.result
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(onClick = { onItemClick(result) }),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
        ) {
            if (showBannerBackground && (bannerModel != null || item.fullBackdropPath != null)) {
                BannerView(
                    bannerModel = bannerModel ?: item.fullBackdropPath?.let { rememberRemoteImageData(it) },
                    blur = Blur.Normal,
                    modifier = Modifier.height(contentHeight.pxToDp() + 24.dp),
                )
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .onGloballyPositioned {
                            contentHeight = it.size.height
                        },
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PosterItem(
                    item = item,
                    modifier = Modifier.width(75.dp),
                )

                Column(
                    modifier = Modifier.weight(1f).wrapContentHeight(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    val titleColor =
                        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
                    Text(
                        text = item.title ?: item.name ?: mokoString(MR.strings.unknown),
                        style = MaterialTheme.typography.titleMedium,
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
                        color = if (showBannerBackground) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (includeOverview && item.overview != null) {
                        Text(
                            text = item.overview ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                if (showBannerBackground) {
                                    Color.White.copy(
                                        alpha = 0.8f,
                                    )
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 8.dp),
                        )
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
) {
    val item = result.result
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .combinedClickable(onClick = { onItemClick(result) }),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
            modifier =
                Modifier
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
                    fontSize = 18.sp,
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

@Composable
private fun MediaDetails(
    item: ArrMedia,
    isActive: Boolean,
    showBannerBackground: Boolean,
) {
    Column {
        when (item) {
            is ArrSeries -> SeriesDetails(item, isActive, showBannerBackground)
            is ArrMovie -> MovieDetails(item, isActive, showBannerBackground)
            is Arrtist -> ArtistDetails(item, isActive, showBannerBackground)
            is Author -> AuthorDetails(item, isActive, showBannerBackground)
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
) {
    val contentColor = if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
    val seasonLabel = mokoPlural(MR.plurals.seasons, item.seasonCount)
    val fileSizeString = item.fileSize?.bytesAsFileSizeString()?.takeUnless { item.id == null }
    val network = item.network

    val secondLine = listOfNotNull(seasonLabel, fileSizeString, network).joinToString(BULLET)
    Text(secondLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    val statusStr =
        when (item.status) {
            MediaStatus.Continuing ->
                item.nextAiring?.format()
                    ?: "${mokoString(item.status.resource)} - ${mokoString(MR.strings.unknown)}"
            else -> mokoString(item.status.resource)
        }
    Text(statusStr, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    if (item.id != null) {
        Text(
            text = "${item.episodeFileCount}/${item.episodeCount}",
            fontSize = 12.sp,
            color = contentColor,
            modifier = Modifier.padding(top = 8.dp, bottom = 1.dp),
        )
        LinearProgressIndicator(
            progress = { item.statusProgress },
            color = if (isActive) ArrPurple else item.statusColor,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(6.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun MovieDetails(
    item: ArrMovie,
    isActive: Boolean,
    showBannerBackground: Boolean,
) {
    val contentColor = if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
    item.releaseDate?.format("MMMM d, yyyy")?.let {
        Text(it, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    }

    val firstLine = listOfNotNull(item.runtimeString, item.studio).joinToString(" • ")
    firstLine.unlessEmpty { firstLine ->
        Text(firstLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    }

    val qualityLabel =
        item.movieFile
            ?.quality
            ?.quality
            ?.name
    val fileSizeLabel = item.fileSize?.bytesAsFileSizeString()?.takeUnless { item.id == null }
    val thirdLine = listOfNotNull(qualityLabel, fileSizeLabel).joinToString(BULLET)
    thirdLine.unlessEmpty { thirdLine ->
        Text(thirdLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun ArtistDetails(
    item: Arrtist,
    isActive: Boolean,
    showBannerBackground: Boolean,
) {
    val contentColor = if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
    val albumLabel = mokoPlural(MR.plurals.albums, item.albumCount)
    val trackLabel = mokoPlural(MR.plurals.tracks, item.trackCount)
    val secondLine = listOfNotNull(albumLabel, trackLabel).joinToString(BULLET)
    Text(secondLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    val statusStr = mokoString(item.status.resource)
    Text(statusStr, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
}

@Composable
private fun AuthorDetails(
    item: Author,
    isActive: Boolean,
    showBannerBackground: Boolean,
) {
    val contentColor = if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface

    val bookLabel = mokoPlural(MR.plurals.books_count, item.bookCount)
    val firstLine = listOfNotNull(bookLabel).joinToString(BULLET)
    Text(firstLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    val statusStr =
        when (item.status) {
            MediaStatus.Continuing ->
                item.nextBook?.releaseDate?.format()
                    ?: "${mokoString(item.status.resource)} - ${mokoString(MR.strings.unknown)}"
            else -> mokoString(item.status.resource)
        }
    Text(statusStr, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    if (item.id != null) {
        Text(
            text = "${item.bookFileCount}/${item.bookCount}",
            fontSize = 12.sp,
            color = contentColor,
            modifier = Modifier.padding(top = 8.dp, bottom = 1.dp),
        )
        LinearProgressIndicator(
            progress = { item.statusProgress },
            color = if (isActive) ArrPurple else item.statusColor,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(6.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
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
    Text(authorString, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    val seriesString =
        item.series?.let {
            if (item.seriesNumber != null) "$it (#${item.seriesNumber})" else it
        }
    val fileSizeString = item.fileSize?.bytesAsFileSizeString()?.takeUnless { item.id == null }

    val secondLine = listOfNotNull(seriesString, fileSizeString, item.publisher).joinToString(BULLET)
    if (secondLine.isNotEmpty()) {
        Text(secondLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    }

    val statusStr = mokoString(item.status.resource)
    Text(statusStr, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
}

@Composable
private fun SearchAudiobookDetails(
    item: SearchAudiobook,
    showBannerBackground: Boolean,
) {
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface

    val authorString = item.authors.joinToString(", ") { it.name }
    Text(authorString, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    val seriesString =
        item.series.firstOrNull()?.let {
            if (it.position != null) "${it.name} (#${it.position})" else it.name
        }
    val secondLine = listOfNotNull(seriesString, item.publisher).joinToString(BULLET)
    if (secondLine.isNotEmpty()) {
        Text(secondLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    }

    item.releaseDate?.format("MMMM d, yyyy")?.let {
        Text(it, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun MockDetails(
    item: MockMedia,
    showBannerBackground: Boolean,
) {
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface

    Text("Mock Studio", color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    Text("Mock Quality", color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
}

@Composable
fun BannerView(
    bannerModel: Any?,
    modifier: Modifier = Modifier,
    blur: Blur = Blur.Normal,
) {
    Box(modifier = modifier) {
        when (bannerModel) {
            is Painter -> {
                Image(
                    painter = bannerModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.5f,
                )
            }

            else -> {
                AsyncImage(
                    model = bannerModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
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
