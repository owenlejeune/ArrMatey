package com.dnfapps.arrmatey.ui.components

import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
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
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrLightPurple
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
import com.skydoves.cloudy.cloudy
import kotlin.time.ExperimentalTime

private val defaultHeight = 100.dp

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
    multiSelectState: MultiSelectState<Long> = MultiSelectState(selectionModeAvailable = false)
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = userScrollEnabled,
        contentPadding = PaddingValues(vertical = 12.dp)
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
                multiSelectState = multiSelectState
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
    isActive: Boolean = false,
    showBannerBackground: Boolean = true,
    includeOverview: Boolean = false,
    posterModel: Any? = null,
    bannerModel: Any? = null,
    blur: Blur = Blur.Normal,
    posterElevation: PosterElevation = PosterElevation.Medium,
    posterRadius: PosterRadius = PosterRadius.Medium,
    multiSelectState: MultiSelectState<Long> = MultiSelectState(selectionModeAvailable = false)
) {
    val isInSelectionMode by multiSelectState.isInSelectionMode.collectAsStateWithLifecycle()
    val selectedItems by multiSelectState.selectedItems.collectAsStateWithLifecycle()
    val isSelected = item.id?.let { selectedItems.contains(it) } ?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .combinedClickable(
                onClick = {
                    if (isInSelectionMode) {
                        item.id?.let { multiSelectState.toggle(it) }
                    } else {
                        onItemClick(item)
                    }
                },
                onLongClick = {
                    if (!isInSelectionMode) {
                        multiSelectState.enterSelectionMode()
                        item.id?.let { multiSelectState.toggle(it) }
                    }
                }
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = if (isSelected) BorderStroke(4.dp, ArrLightPurple) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            if (showBannerBackground) {
                BannerView(
                    blur = blur,
                    bannerModel = bannerModel ?: item.getBanner()?.remoteUrl?.let {
                        rememberRemoteImageData(it)
                    },
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(TranslucentBlack)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    PosterItem(
                        item = item,
                        aspectRatio = aspectRatio,
                        modifier = Modifier.height(defaultHeight),
                        posterModel = posterModel,
                        elevation = posterElevation,
                        radius = posterRadius,
                        multiSelectState = multiSelectState
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val titleColor =
                                if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
                            Text(
                                text = buildString {
                                    append(item.title ?: mokoString(MR.strings.unknown))
                                    item.year?.let { year ->
                                        item.title?.contains("$year")?.let {
                                            if (!it) append(" ($year)")
                                        }
                                    }
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            item.id?.let {
                                Icon(
                                    imageVector = if (item.monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = titleColor
                                )
                            }
                            if (isInSelectionMode) {
                                CircularCheckbox(
                                    checked = isSelected,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        MediaDetails(item, isActive, showBannerBackground)
                    }
                }

                AnimatedVisibility(
                    visible = includeOverview && item.overview != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Text(
                        text = Html.fromHtml(item.overview!!, Html.FROM_HTML_MODE_COMPACT).toString(),
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
    showBannerBackground: Boolean
) {
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

@Composable
private fun MockDetails(
    item: MockMedia,
    showBannerBackground: Boolean
) {
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface
    Text(item.detailString, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    Text("Status: ${item.statusString}", color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    LinearProgressIndicator(
        progress = { item.statusProgress },
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .height(6.dp),
        color = ArrBlue,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@OptIn(ExperimentalTime::class)
@Composable
private fun SeriesDetails(
    item: ArrSeries,
    isActive: Boolean,
    showBannerBackground: Boolean
) {
    val seasonLabel = mokoPlural(MR.plurals.seasons, item.seasonCount)
    val fileSizeString = item.fileSize.bytesAsFileSizeString()
    val network = item.network
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface

    val firstLine = listOfNotNull(network, seasonLabel, fileSizeString).joinToString(Bullet)
    Text(firstLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    val statusStr = when (item.status) {
        MediaStatus.Continuing -> item.nextAiring?.format()
            ?: "${mokoString(item.status.resource)} - ${mokoString(MR.strings.unknown)}"
        else -> mokoString(item.status.resource)
    }
    Text(statusStr, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    if (item.id != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 1.dp)
        ) {
            Text(text = item.episodeFileCount.toString(), fontSize = 12.sp, color = contentColor)
            Text(text = "/${item.episodeCount}", fontSize = 12.sp, color = contentColor)
        }
        LinearProgressIndicator(
            progress = { item.statusProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = if (isActive) ArrPurple else item.statusColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun MovieDetails(
    item: ArrMovie,
    isActive: Boolean,
    showBannerBackground: Boolean
) {
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface

    item.releaseDate?.format()?.let {
        Text(it, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    }

    val firstLine = listOfNotNull(item.runtimeString, item.studio).joinToString(" • ")
    Text(firstLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    val statusLabel = item.status.name.takeIf { item.fileSize == 0L }
    val secondLine = listOfNotNull(
        statusLabel,
        item.fileSize.bytesAsFileSizeString(),
        item.movieFile?.quality?.quality?.name
    ).joinToString(" • ")
    Text(secondLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    if (item.id != null) {
        LinearProgressIndicator(
            progress = { if (isActive) 1f else item.statusProgress },
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(6.dp),
            color = if (isActive) ArrPurple else item.statusColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun ArtistDetails(
    item: Arrtist,
    isActive: Boolean,
    showBannerBackground: Boolean
) {
    val albumCountString = mokoPlural(MR.plurals.albums, item.albumCount)
    val fileSizeString = item.fileSize.bytesAsFileSizeString()
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface

    val firstLine = listOfNotNull(albumCountString, fileSizeString).joinToString(Bullet)
    Text(firstLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    val statusStr = when (item.status) {
        MediaStatus.Continuing -> item.nextAlbum?.releaseDate?.format()
            ?: "${mokoString(item.status.resource)} - ${mokoString(MR.strings.unknown)}"
        else -> mokoString(item.status.resource)
    }
    Text(statusStr, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    if (item.id != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 1.dp)
        ) {
            Text(text = item.trackFileCount.toString(), fontSize = 12.sp, color = contentColor)
            Text(text = "/${item.trackCount}", fontSize = 12.sp, color = contentColor)
        }
        LinearProgressIndicator(
            progress = { item.statusProgress },
            color = if (isActive) ArrPurple else item.statusColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AuthorDetails(
    item: Author,
    isActive: Boolean,
    showBannerBackground: Boolean
) {
    val bookCountString = mokoPlural(MR.plurals.books_count, item.totalBookCount)
    val fileSizeString = item.fileSize.bytesAsFileSizeString()
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface

    val firstLine = listOfNotNull(bookCountString, fileSizeString).joinToString(Bullet)
    Text(firstLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    val statusStr = when (item.status) {
        MediaStatus.Continuing -> item.nextBook?.releaseDate?.format()
            ?: "${mokoString(item.status.resource)} - ${mokoString(MR.strings.unknown)}"
        else -> mokoString(item.status.resource)
    }
    Text(statusStr, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    if (item.id != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 1.dp)
        ) {
            Text(text = item.bookFileCount.toString(), fontSize = 12.sp, color = contentColor)
            Text(text = "/${item.bookCount}", fontSize = 12.sp, color = contentColor)
        }
        LinearProgressIndicator(
            progress = { item.statusProgress },
            color = if (isActive) ArrPurple else item.statusColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun AudiobookDetails(
    item: Audiobook,
    isActive: Boolean,
    showBannerBackground: Boolean
) {
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface

    val authorString = item.authors.joinToString(", ")
    Text(authorString, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    val seriesString = item.series?.let {
        if (item.seriesNumber != null) "$it (#${item.seriesNumber})" else it
    }
    val fileSizeString = item.fileSize.bytesAsFileSizeString().takeIf { item.fileSize > 0 }

    val secondLine = listOfNotNull(seriesString, fileSizeString, item.publisher).joinToString(Bullet)
    if (secondLine.isNotEmpty()) {
        Text(secondLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    }

    val statusStr = mokoString(item.status.resource)
    Text(statusStr, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    if (item.id != null) {
        LinearProgressIndicator(
            progress = { if (isActive) 1f else item.statusProgress },
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(6.dp),
            color = if (isActive) ArrPurple else item.statusColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun SearchAudiobookDetails(
    item: SearchAudiobook,
    showBannerBackground: Boolean
) {
    val contentColor =
        if (showBannerBackground) Color.White else MaterialTheme.colorScheme.onSurface

    val authorString = item.authors.joinToString(", ") { it.name }
    Text(authorString, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)

    val narratorString = item.narrators.joinToString(", ") { it.name }
    Text(
        text = mokoString(MR.strings.narrated_by, narratorString),
        color = contentColor, fontSize = 14.sp, lineHeight = 18.sp
    )

    val seriesString = item.seriesList.joinToString(", ")
    val secondLine = listOfNotNull(seriesString, item.publisher, item.runtimeString)
        .joinToString(Bullet)
    if (secondLine.isNotEmpty()) {
        Text(secondLine, color = contentColor, fontSize = 14.sp, lineHeight = 18.sp)
    }
}

@Composable
fun BannerView(
    bannerModel: Any?,
    modifier: Modifier = Modifier,
    blur: Blur = Blur.Normal
) {
    when (bannerModel) {
        null -> {}
        is Painter -> {
            Box(modifier = modifier) {
                Image(
                    painter = bannerModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .cloudy(radius = blur.radius)
                )
            }
        }
        else -> {
            Box(modifier = modifier) {
                AsyncImage(
                    model = bannerModel,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .cloudy(radius = blur.radius)
                )
            }
        }
    }
}
