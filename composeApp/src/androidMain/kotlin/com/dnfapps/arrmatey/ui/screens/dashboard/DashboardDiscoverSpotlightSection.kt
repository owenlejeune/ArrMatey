package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.MediaStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.StatusOverlay
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DashboardDiscoverSpotlightSection(
    state: CombinedDashboardState.Success,
    onMediaClick: (tmdbId: Long, requestType: RequestType) -> Unit,
    onRequestClick: ((DiscoverResult) -> Unit)? = null,
    isEditing: Boolean = false,
    enabled: Boolean = true,
) {
    val spotlightItems = state.spotlightMedia

    val pagerState = rememberPagerState(pageCount = { spotlightItems.size })

    LaunchedEffect(pagerState, spotlightItems.size, enabled) {
        if (spotlightItems.size > 1 && enabled) {
            while (true) {
                delay(10000.milliseconds)
                if (!pagerState.isScrollInProgress && spotlightItems.isNotEmpty()) {
                    val nextPage = (pagerState.currentPage + 1) % spotlightItems.size
                    pagerState.animateScrollToPage(page = nextPage)
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column {
            AnimatedVisibility(
                visible = isEditing || state.seerrInstances.isEmpty() || spotlightItems.isEmpty()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(InstanceType.Seerr.icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = mokoString(MR.strings.dashboard_discover_spotlight),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (state.seerrInstances.isEmpty() || spotlightItems.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_type_instances_message, InstanceType.Seerr.name),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp, bottom = 36.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                val currentItem = spotlightItems.getOrNull(pagerState.currentPage) ?: spotlightItems.first()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(MaterialTheme.shapes.large),
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = enabled && spotlightItems.size > 1,
                            modifier = Modifier.fillMaxSize(),
                        ) { page ->
                            val spotlightItem = spotlightItems[page]
                            val title = spotlightItem.title ?: spotlightItem.name ?: mokoString(MR.strings.unknown)

                            val imageModel: Any =
                                spotlightItem.fullBackdropPath
                                    ?: spotlightItem.fullPosterPath
                                    ?: if (spotlightItem.mediaType == RequestType.Tv) {
                                        painterResource(MR.images.sonarr_mock_poster)
                                    } else {
                                        painterResource(MR.images.radarr_mock_poster)
                                    }

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .clickable(enabled = enabled) {
                                            onMediaClick(spotlightItem.id, spotlightItem.mediaType)
                                        },
                            ) {
                                if (imageModel is Painter) {
                                    Image(
                                        painter = imageModel,
                                        contentDescription = title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                } else {
                                    AsyncImage(
                                        model = imageModel,
                                        contentDescription = title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors =
                                                listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.4f),
                                                    Color.Black.copy(alpha = 0.85f),
                                                ),
                                        ),
                                    ),
                        )

                        AnimatedContent(
                            targetState = currentItem,
                            transitionSpec = {
                                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                            },
                            label = "SpotlightBannerOverlayTransition",
                            modifier = Modifier.fillMaxSize(),
                        ) { item ->
                            val title = item.title ?: item.name ?: mokoString(MR.strings.unknown)
                            val year = (item.releaseDate ?: item.firstAirDate)?.take(4)
                            val voteAverage = item.voteAverage

                            Box(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Row(
                                    modifier =
                                        Modifier
                                            .align(Alignment.TopStart)
                                            .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ) {
                                        Text(
                                            text = mokoString(MR.strings.dashboard_discover_spotlight),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        )
                                    }

                                    item.mediaInfo?.let { info ->
                                        StatusOverlay(MediaStatus.fromValue(info.status))
                                    }
                                }

                                Column(
                                    modifier =
                                        Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (year != null) {
                                            Surface(
                                                shape = MaterialTheme.shapes.extraSmall,
                                                color = Color.Black.copy(alpha = 0.6f),
                                            ) {
                                                Text(
                                                    text = year,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                )
                                            }
                                        }

                                        if (voteAverage > 0.0) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFB800),
                                                    modifier = Modifier.size(14.dp),
                                                )
                                                Text(
                                                    text = "${(voteAverage * 10).toInt() / 10.0}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = currentItem,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "SpotlightContentTransition",
                    ) { item ->
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = item.overview ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3,
                                minLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        if (enabled) {
                                            onMediaClick(item.id, item.mediaType)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = enabled,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(text = mokoString(MR.strings.details))
                                }

                                val mediaStatus = state.resolveMediaStatus(item)
                                val (buttonLabel, buttonIcon, isTonal) =
                                    when (mediaStatus) {
                                        MediaStatus.Available -> {
                                            if (item.mediaType == RequestType.Tv) {
                                                Triple(MR.strings.request_more, Icons.Default.Add, false)
                                            } else {
                                                Triple(MR.strings.available, Icons.Default.Check, true)
                                            }
                                        }
                                        MediaStatus.Pending -> Triple(MR.strings.pending, Icons.Default.Schedule, true)
                                        MediaStatus.Processing -> Triple(MR.strings.processing, Icons.Default.Schedule, true)
                                        MediaStatus.PartiallyAvailable -> Triple(MR.strings.request_more, Icons.Default.Add, false)
                                        else -> Triple(MR.strings.request, Icons.Default.Add, false)
                                    }

                                if (isTonal) {
                                    FilledTonalButton(
                                        onClick = {
                                            if (enabled) {
                                                if (onRequestClick != null) {
                                                    onRequestClick(item)
                                                } else {
                                                    onMediaClick(item.id, item.mediaType)
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = enabled,
                                    ) {
                                        Icon(
                                            imageVector = buttonIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(text = mokoString(buttonLabel))
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (enabled) {
                                                if (onRequestClick != null) {
                                                    onRequestClick(item)
                                                } else {
                                                    onMediaClick(item.id, item.mediaType)
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = enabled,
                                    ) {
                                        Icon(
                                            imageVector = buttonIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(text = mokoString(buttonLabel))
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
