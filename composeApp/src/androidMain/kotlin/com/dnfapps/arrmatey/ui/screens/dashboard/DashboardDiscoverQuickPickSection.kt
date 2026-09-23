package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.DiscoverResult
import com.dnfapps.arrmatey.seerr.api.model.MediaStatus
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.MediaRequestTypeChip
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun DashboardDiscoverQuickPickSection(
    state: CombinedDashboardState.Success,
    onShuffleClick: () -> Unit,
    onMediaClick: (tmdbId: Long, requestType: RequestType) -> Unit,
    onRequestClick: ((DiscoverResult) -> Unit)? = null,
    isEditing: Boolean = false,
    enabled: Boolean = true,
) {
    val currentItem = state.quickPickItem ?: state.quickPickMedia.firstOrNull()

    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationDegrees,
        animationSpec = tween(durationMillis = 400),
        label = "ShuffleRotation",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(
                visible = isEditing || state.seerrInstances.isEmpty(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(InstanceType.Seerr.icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = mokoString(MR.strings.dashboard_discover_quick_pick),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (state.seerrInstances.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_type_instances_message, InstanceType.Seerr.name),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else if (currentItem == null) {
                Text(
                    text = mokoString(MR.strings.no_media_found),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                AnimatedContent(
                    targetState = currentItem,
                    transitionSpec = {
                        (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.92f)) togetherWith
                            (fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.92f))
                    },
                    label = "QuickPickTransition",
                ) { item ->
                    val title = item.title ?: item.name ?: mokoString(MR.strings.unknown)
                    val year = (item.releaseDate ?: item.firstAirDate)?.take(4)
                    val voteAverage = item.voteAverage

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            PosterItem(
                                item = item,
                                modifier = Modifier.fillMaxHeight(),
                                showFooter = false,
                                showOverlays = false
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    MediaRequestTypeChip(
                                        text = mokoString(if (item.mediaType == RequestType.Tv) MR.strings.series else MR.strings.movie),
                                        requestType = item.mediaType,
                                    )

                                    if (year != null) {
                                        Surface(
                                            shape = MaterialTheme.shapes.extraSmall,
                                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                        ) {
                                            Text(
                                                text = year,
                                                style = MaterialTheme.typography.labelSmall,
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
                                            )
                                        }
                                    }
                                }

                                item.overview?.let { overview ->
                                    if (overview.isNotBlank()) {
                                        Text(
                                            text = overview,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    if (enabled) {
                                        rotationDegrees += 360f
                                        onShuffleClick()
                                    }
                                },
                                enabled = enabled && state.quickPickMedia.isNotEmpty(),
                                colors =
                                    IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    ),
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = mokoString(MR.strings.shuffle),
                                    modifier =
                                        Modifier
                                            .size(20.dp)
                                            .rotate(animatedRotation),
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    if (enabled) {
                                        onMediaClick(item.id, item.mediaType)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = enabled,
                            ) {
                                Text(
                                    text = mokoString(MR.strings.details),
                                    style = MaterialTheme.typography.labelMedium,
                                )
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
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = mokoString(buttonLabel),
                                        style = MaterialTheme.typography.labelMedium,
                                    )
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
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = mokoString(buttonLabel),
                                        style = MaterialTheme.typography.labelMedium,
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
