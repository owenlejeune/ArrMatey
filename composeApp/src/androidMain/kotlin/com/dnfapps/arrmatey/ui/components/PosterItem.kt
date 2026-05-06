package com.dnfapps.arrmatey.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.utils.AspectRatio
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
    additionalContent: @Composable BoxScope.() -> Unit = {}
) {
    var imageLoadError by remember { mutableStateOf(false) }

    val model = posterModel ?: rememberRemoteImageData(
        url = item.getPoster()?.remoteUrl,
        onError = { _, err ->
            println(err.throwable.message)
            imageLoadError = true
        }
    )

    BasePosterItem(
        model = model,
        modifier = modifier,
        enabled = enabled,
        elevation = elevation,
        radius = radius,
        posterHeight = posterHeight,
        aspectRatio = aspectRatio,
        onClick = {
            onItemClick?.invoke(item)
        },
        additionalContent = {
            additionalContent()
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

@Composable
fun PosterItem(
    item: RequestMediaDetails,
    modifier: Modifier = Modifier,
    elevation: PosterElevation = PosterElevation.Medium,
    radius: PosterRadius = PosterRadius.Medium,
    posterHeight: Dp? = null,
    aspectRatio: AspectRatio = AspectRatio.Poster,
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
        elevation = elevation,
        radius = radius,
        posterHeight = posterHeight,
        aspectRatio = aspectRatio,
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
fun BasePosterItem(
    model: Any,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    elevation: PosterElevation = PosterElevation.Medium,
    radius: PosterRadius = PosterRadius.Medium,
    posterHeight: Dp? = null,
    aspectRatio: AspectRatio = AspectRatio.Poster,
    onClick: (() -> Unit)? = null,
    errorContent: @Composable BoxScope.() -> Unit = {},
    additionalContent: @Composable BoxScope.() -> Unit = {},
    footerContent: @Composable ColumnScope.() -> Unit = {},
    footerVisible: Boolean = true
) {
    Card(
        shape = RoundedCornerShape(radius.radius),
        elevation = CardDefaults.cardElevation(elevation.elevation),
        modifier = modifier,
        onClick = {
            onClick?.invoke()
        },
        enabled = enabled && onClick != null
    ) {
        Column {
            Box(
                modifier = Modifier
                    .then(posterHeight?.let { Modifier.height(it) } ?: Modifier)
                    .aspectRatio(aspectRatio.ratio, true)
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