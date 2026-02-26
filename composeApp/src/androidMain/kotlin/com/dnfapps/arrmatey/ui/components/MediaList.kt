package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.ArrPurple
import com.dnfapps.arrmatey.ui.theme.TranslucentBlack
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoPlural
import com.dnfapps.arrmatey.utils.mokoString
import com.skydoves.cloudy.cloudy
import kotlin.time.ExperimentalTime

private val defaultHeight = 100.dp

@Composable
fun <T: ArrMedia> MediaList(
    aspectRatio: AspectRatio,
    items: List<T>,
    onItemClick: (T) -> Unit,
    itemIsActive: (T) -> Boolean,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = userScrollEnabled,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(items) { item ->
            val isActive = itemIsActive(item)
            MediaItem(aspectRatio, item, onItemClick, isActive)
        }
    }
}

@Composable
fun <T: ArrMedia> MediaItem(
    aspectRatio: AspectRatio,
    item: T,
    onItemClick: (T) -> Unit,
    isActive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onItemClick(item)
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            BannerView(
                bannerUrl = item.getBanner()?.remoteUrl,
                modifier = Modifier.matchParentSize()
            )
            Box(modifier = Modifier.matchParentSize().background(TranslucentBlack))

            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                val posterUrl = item.getPoster()?.remoteUrl
                AsyncImage(
                    model = rememberRemoteImageData(posterUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .height(defaultHeight)
                        .aspectRatio(aspectRatio.ratio, true)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )

                Column(
                    modifier = Modifier.defaultMinSize(minHeight = defaultHeight)
                ) {
                    Text(
                        text = buildString {
                            append(item.title ?: mokoString(MR.strings.unknown))
                            item.year?.let { year ->
                                item.title?.contains("$year")?.let {
                                    if (!it) {
                                        append(" ($year)")
                                    }
                                }
                            }
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    MediaDetails(item, isActive)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.MediaDetails(item: ArrMedia, isActive: Boolean) {
    when (item) {
        is ArrSeries -> SeriesDetails(item, isActive)
        is ArrMovie -> MovieDetails(item)
        is Arrtist -> ArtistDetails(item, isActive)
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun ColumnScope.SeriesDetails(
    item: ArrSeries,
    isActive: Boolean
) {
    val seasonLabel = mokoPlural(MR.plurals.seasons, item.seasonCount)
    val fileSizeString = item.fileSize.bytesAsFileSizeString()
    val network = item.network

    val firstLine = listOfNotNull(network, seasonLabel, fileSizeString)
        .joinToString(Bullet)
    Text(firstLine, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)

    val statusStr = when (item.status) {
        MediaStatus.Continuing -> item.nextAiring?.format()
            ?: "${mokoString(item.status.resource)} - ${mokoString(MR.strings.unknown)}"
        else -> mokoString(item.status.resource)
    }
    Text(statusStr, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)

    if (item.id != null) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 1.dp)
        ) {
            Text(text = item.episodeFileCount.toString(), fontSize = 12.sp, color = Color.White)
            Text(text = "/${item.episodeCount}", fontSize = 12.sp, color = Color.White)
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
private fun ColumnScope.MovieDetails(item: ArrMovie) {
    item.releaseDate?.format()?.let {
        Text(it, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)
    }

    val firstLine = listOfNotNull(item.runtimeString, item.studio).joinToString(" • ")
    Text(firstLine, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)

    val statusLabel = item.status.name.takeIf { item.fileSize == 0L }
    val secondLine = listOfNotNull(statusLabel, item.fileSize.bytesAsFileSizeString(), item.movieFile?.quality?.quality?.name).joinToString(" • ")
    Text(secondLine, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)

    if (item.id != null) {
        Spacer(modifier = Modifier.weight(1f))
        LinearProgressIndicator(
            progress = { item.statusProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = item.statusColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun ColumnScope.ArtistDetails(
    item: Arrtist,
    isActive: Boolean
) {
    val albumCountString = mokoPlural(MR.plurals.albums, item.albumCount)
    val fileSizeString = item.fileSize.bytesAsFileSizeString()

    val firstLine = listOfNotNull(albumCountString, fileSizeString)
        .joinToString(Bullet)
    Text(firstLine, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)

    val statusStr = when (item.status) {
        MediaStatus.Continuing -> item.nextAlbum?.releaseDate?.format()
            ?: "${mokoString(item.status.resource)} - ${mokoString(MR.strings.unknown)}"
        else -> mokoString(item.status.resource)
    }
    Text(statusStr, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp)

    if (item.id != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 1.dp)
        ) {
            Text(text = item.trackFileCount.toString(), fontSize = 12.sp, color = Color.White)
            Text(text = "/${item.trackCount}", fontSize = 12.sp, color = Color.White)
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
fun BannerView(
    bannerUrl: String?,
    modifier: Modifier = Modifier
) {
    bannerUrl?.let { bannerUrl ->
        AsyncImage(
            model = rememberRemoteImageData(bannerUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.cloudy()
        )
    }
}