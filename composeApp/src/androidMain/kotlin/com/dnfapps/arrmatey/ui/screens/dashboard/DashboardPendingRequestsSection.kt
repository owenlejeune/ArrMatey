package com.dnfapps.arrmatey.ui.screens.dashboard

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.arr.state.CombinedDashboardState
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.BannerView
import com.dnfapps.arrmatey.ui.components.MediaRequestTypeChip
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.screens.requests.StatusChip
import com.dnfapps.arrmatey.ui.screens.requests.UserInfoRow
import com.dnfapps.arrmatey.ui.theme.TranslucentBlack
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.mokoString

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
            if (pendingRequests.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_requests_found),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
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
                .widthIn(max = 260.dp)
                .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Box(modifier = Modifier.wrapContentWidth()) {
            BannerView(
                bannerModel = details?.fullBackdropPath?.let { rememberRemoteImageData(it) },
                modifier = Modifier.matchParentSize(),
            )
            Box(modifier = Modifier.matchParentSize().background(TranslucentBlack))

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    AsyncImage(
                        model = rememberRemoteImageData(details?.fullPosterPath),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .height(80.dp)
                                .aspectRatio(AspectRatio.Poster.ratio, true)
                                .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit,
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            details?.displayDate?.year?.let { year ->
                                Text(
                                    text = year.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                            MediaRequestTypeChip(text = request.type.name, request.type)
                        }

                        Text(
                            text = details?.displayTitle ?: mokoString(MR.strings.unknown),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        StatusChip(request)
                    }
                }
                UserInfoRow(
                    label = mokoString(MR.strings.requested_by),
                    displayName = request.requestedBy.displayName,
                    avatar = request.requestedBy.avatar,
                )
            }
        }
    }
}
