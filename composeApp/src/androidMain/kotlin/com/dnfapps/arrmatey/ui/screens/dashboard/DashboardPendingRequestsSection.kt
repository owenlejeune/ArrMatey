package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.MediaRequestTypeChip
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.screens.requests.StatusChip
import com.dnfapps.arrmatey.ui.screens.requests.UserInfoRow
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun DashboardPendingRequestsSection(
    state: CombinedDashboardState.Success,
    enabled: Boolean = true,
    onRequestClick: (MediaRequestPackage) -> Unit = {},
) {
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
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Inbox,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = mokoString(MR.strings.requests),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            val pendingRequests = state.pendingRequests
            if (state.seerrInstances.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_type_instances_message, InstanceType.Seerr.name),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else if (pendingRequests.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_requests_found),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    items(pendingRequests, key = { it.request.id }) { item ->
                        CompactRequestCard(
                            mediaPackage = item,
                            onClick = { if (enabled) onRequestClick(item) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompactRequestCard(
    mediaPackage: MediaRequestPackage,
    onClick: () -> Unit,
) {
    val request = mediaPackage.request
    val details = mediaPackage.details

    Card(
        modifier =
            Modifier
                .width(280.dp)
                .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                val posterModel =
                    if (details?.fullPosterPath != null) {
                        rememberRemoteImageData(details.fullPosterPath)
                    } else {
                        null
                    }

                if (posterModel != null) {
                    AsyncImage(
                        model = posterModel,
                        contentDescription = null,
                        modifier =
                            Modifier
                                .width(60.dp)
                                .aspectRatio(AspectRatio.Poster.ratio)
                                .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .width(60.dp)
                                .aspectRatio(AspectRatio.Poster.ratio)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        MediaRequestTypeChip(text = request.type.name, request.type)
                        details?.displayDate?.year?.let { year ->
                            Text(
                                text = year.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Text(
                        text = details?.displayTitle ?: mokoString(MR.strings.unknown),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    StatusChip(request)
                }
            }

            UserInfoRow(
                label = mokoString(MR.strings.requested_by),
                displayName = request.requestedBy.displayName,
                avatar = request.requestedBy.avatar,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
