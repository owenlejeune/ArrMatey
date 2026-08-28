package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.MediaStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.ArrLightPurple
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.MultiSelectState
import com.dnfapps.arrmatey.utils.PosterElevation
import com.dnfapps.arrmatey.utils.PosterRadius
import com.dnfapps.arrmatey.utils.mokoString
import com.skydoves.cloudy.cloudy

@Composable
fun PosterItem(
    item: ArrMedia,
    modifier: Modifier = Modifier,
    showFooter: Boolean = false,
    onItemClick: ((ArrMedia) -> Unit)? = null,
    enabled: Boolean = true,
    elevation: PosterElevation = PosterElevation.Medium,
    radius: PosterRadius = PosterRadius.Medium,
    posterHeight: Dp? = null,
    aspectRatio: AspectRatio = AspectRatio.Poster,
    posterModel: Any? = null,
    additionalContent: @Composable BoxScope.() -> Unit = {},
    multiSelectState: MultiSelectState<Long> = MultiSelectState(selectionModeAvailable = false)
) {
    val isInSelectionMode by multiSelectState.isInSelectionMode.collectAsStateWithLifecycle()
    val selectedItems by multiSelectState.selectedItems.collectAsStateWithLifecycle()
    val isSelected = item.id?.let { selectedItems.contains(it) } ?: false

    var imageLoadError by remember { mutableStateOf(value = false) }

    val model = posterModel ?: rememberRemoteImageData(
        url = item.getPoster()?.remoteUrl,
        onError = { _, err ->
            println(err.throwable.message)
            imageLoadError = true
        },
        onSuccess = { _, _ ->
            imageLoadError = false
        }
    )

    BasePosterItem(
        model = model,
        modifier = modifier,
        isSelected = isSelected,
        enabled = enabled,
        elevation = elevation,
        radius = radius,
        posterHeight = posterHeight,
        aspectRatio = aspectRatio,
        onClick = {
            if (isInSelectionMode) {
                item.id?.let { multiSelectState.toggle(it) }
            } else {
                onItemClick?.invoke(item)
            }
        },
        onLongClick = {
            if (!isInSelectionMode) {
                multiSelectState.enterSelectionMode()
                item.id?.let { multiSelectState.toggle(it) }
            }
        },
        additionalContent = {
            additionalContent()
            if (isInSelectionMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    CircularCheckbox(checked = isSelected)
                }
            }
        },
        errorContent = {
            if (imageLoadError) {
                Column (
                    modifier = Modifier.align(Alignment.Center).padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = item.title ?: mokoString(MR.strings.unknown),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        footerVisible = showFooter,
        footerContent = {
            Text(
                text = item.title ?: mokoString(MR.strings.unknown),
                style = MaterialTheme.typography.labelLarge,
                minLines = 2,
                maxLines = 2
            )
            item.year?.let { year ->
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PosterItem(
    item: DiscoverResult,
    modifier: Modifier = Modifier,
    onItemClick: ((DiscoverResult) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    elevation: PosterElevation = PosterElevation.Medium,
    radius: PosterRadius = PosterRadius.Medium,
    posterHeight: Dp? = null,
    aspectRatio: AspectRatio = AspectRatio.Poster,
    isSelected: Boolean = false,
    showOverlays: Boolean = true,
    includeCredits: Boolean = false
) {
    if (item.mediaType == RequestType.Person) {
        CastCrewItem(
            profilePath = item.fullPosterPath,
            name = item.title ?: item.name ?: mokoString(MR.strings.unknown),
            credit = item.knownForDepartment ?: "",
            modifier = modifier.clickable(enabled = onItemClick != null) {
                onItemClick?.invoke(item)
            }
        )
    } else {
        var imageLoadError by remember { mutableStateOf(false) }

        val model = rememberRemoteImageData(
            url = item.fullPosterPath,
            onError = { _, err ->
                println(err.throwable.message)
                imageLoadError = true
            }
        )

        BasePosterItem(
            model = model,
            modifier = modifier,
            isSelected = isSelected,
            elevation = elevation,
            radius = radius,
            posterHeight = posterHeight,
            aspectRatio = aspectRatio,
            onClick = {
                onItemClick?.invoke(item)
            },
            onLongClick = onLongClick,
            additionalContent = {
                if (showOverlays) {
                    MediaTypeOverlay(item.mediaType)
                    item.mediaInfo?.let { info ->
                        StatusOverlay(MediaStatus.fromValue(info.status))
                    }
                }
            },
            footerVisible = true,
            footerContent = {
                Text(
                    text = item.title ?: item.name ?: mokoString(MR.strings.unknown),
                    style = MaterialTheme.typography.labelLarge,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val subText = when (item.mediaType) {
                    RequestType.Person -> item.knownForDepartment ?: ""
                    else -> (item.releaseDate ?: item.firstAirDate)?.take(4) ?: ""
                }
                Text(
                    text = subText,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )

                if (includeCredits) {
                    val credit = (item.character ?: item.job) ?: ""
                    Text(
                        text = credit,
                        style = MaterialTheme.typography.labelMediumEmphasized,
                        maxLines = 2,
                        minLines = 2
                    )
                }
            },
            errorContent = {
                if (imageLoadError) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = item.title ?: item.name ?: mokoString(MR.strings.unknown),
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun PosterItem(
    item: RequestMediaDetails,
    modifier: Modifier = Modifier,
    onItemClick: ((RequestMediaDetails) -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    elevation: PosterElevation = PosterElevation.Medium,
    radius: PosterRadius = PosterRadius.Medium,
    posterHeight: Dp? = null,
    aspectRatio: AspectRatio = AspectRatio.Poster,
    isSelected: Boolean = false,
    showOverlays: Boolean = true
) {
    var imageLoadError by remember { mutableStateOf(false) }

    val model = rememberRemoteImageData(
        url = item.fullPosterPath,
        onError = { _, err ->
            println(err.throwable.message)
            imageLoadError = true
        }
    )

    BasePosterItem(
        model = model,
        modifier = modifier,
        isSelected = isSelected,
        elevation = elevation,
        radius = radius,
        posterHeight = posterHeight,
        aspectRatio = aspectRatio,
        onClick = {
            onItemClick?.invoke(item)
        },
        onLongClick = onLongClick,
        additionalContent = {
            if (showOverlays) {
                MediaTypeOverlay(item.requestType)
                item.mediaInfo?.let { info ->
                    StatusOverlay(MediaStatus.fromValue(info.status))
                }
            }
        },
        errorContent = {
            if (imageLoadError) {
                Column (
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = item.displayTitle,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}

@Composable
private fun BoxScope.MediaTypeOverlay(type: RequestType) {
    val text = when (type) {
        RequestType.Movie -> mokoString(MR.strings.type_movie)
        RequestType.Tv -> mokoString(MR.strings.type_series)
        RequestType.Person -> mokoString(MR.strings.type_person)
    }.replaceFirstChar { it.uppercase() }

    MediaRequestTypeChip(
        text = text,
        requestType = type,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(8.dp)
    )
}

@Composable
private fun BoxScope.StatusOverlay(status: MediaStatus) {
    val (icon, color) = when (status) {
        MediaStatus.Available -> Icons.Default.CheckCircle to Color(0xFF50d27d)
        MediaStatus.PartiallyAvailable -> Icons.Default.RemoveCircle to Color(0xFFfbbf24)
        MediaStatus.Pending, MediaStatus.Processing -> Icons.Default.Schedule to Color(0xFF3b82f6)
        else -> return
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .size(20.dp)
            .background(Color.White, CircleShape)
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BasePosterItem(
    model: Any,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    elevation: PosterElevation = PosterElevation.Medium,
    radius: PosterRadius = PosterRadius.Medium,
    posterHeight: Dp? = null,
    aspectRatio: AspectRatio = AspectRatio.Poster,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    errorContent: @Composable BoxScope.() -> Unit = {},
    additionalContent: @Composable BoxScope.() -> Unit = {},
    footerContent: @Composable ColumnScope.() -> Unit = {},
    footerVisible: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(radius.radius),
        elevation = CardDefaults.cardElevation(elevation.elevation),
        modifier = modifier.then(
            if (onClick != null || onLongClick != null) {
                Modifier.combinedClickable(
                    enabled = enabled,
                    onClick = { onClick?.invoke() },
                    onLongClick = onLongClick
                )
            } else Modifier
        ),
        border = if (isSelected) BorderStroke(4.dp, ArrLightPurple) else null
    ) {
        val isFixedSize = posterHeight != null
        Column(modifier = if (isFixedSize) Modifier.width(IntrinsicSize.Min) else Modifier) {
            Box(
                modifier = Modifier
                    .then(if (isFixedSize) Modifier.height(posterHeight) else Modifier)
                    .aspectRatio(aspectRatio.ratio, isFixedSize)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                when (model) {
                    is Painter -> Image(
                        painter = model,
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier
                            .cloudy(20)
                            .align(Alignment.Center)
                            .fillMaxSize()
                    )

                    else -> AsyncImage(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .cloudy(20)
                            .align(Alignment.Center)
                            .fillMaxSize()
                    )
                }
                when (model) {
                    is Painter -> Image(
                        painter = model,
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight,
                        modifier = Modifier.align(Alignment.Center)
                            .fillMaxSize()
                    )

                    else -> AsyncImage(
                        model = model,
                        contentDescription = null,
                        contentScale = ContentScale.FillHeight,
                        modifier = Modifier.align(Alignment.Center)
                            .fillMaxSize()
                    )
                }

                errorContent()
                additionalContent()
            }
            AnimatedVisibility(
                visible = footerVisible,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp)
                        .padding(top = 16.dp)
                ) {
                    footerContent()
                }
            }
        }
    }
}
