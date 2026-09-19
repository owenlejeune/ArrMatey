@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestSeason
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.SeerrUser
import com.dnfapps.arrmatey.seerr.api.model.UserPermission
import com.dnfapps.arrmatey.seerr.state.RequestOperationsState
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.BannerView
import com.dnfapps.arrmatey.ui.components.MediaRequestTypeChip
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.TranslucentBlack
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun RequestCard(
    mediaPackage: MediaRequestPackage,
    user: SeerrUser?,
    requestOperationsState: RequestOperationsState,
    onApproveClicked: () -> Unit,
    onDeclineClicked: () -> Unit,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onRemoveFromServiceClicked: () -> Unit,
    onClick: () -> Unit,
    onViewRequestClicked: (() -> Unit)? = null,
) {
    val request = mediaPackage.request
    val details = mediaPackage.details

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = Color.White,
            ),
        onClick = onClick,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            BannerView(
                bannerModel = details?.fullBackdropPath?.let { rememberRemoteImageData(it) },
                modifier = Modifier.matchParentSize(),
            )
            Box(modifier = Modifier.matchParentSize().background(TranslucentBlack))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
            ) {
                RequestCardHeader(
                    posterUrl = details?.fullPosterPath,
                    year = details?.displayDate?.year?.toString() ?: "",
                    requestType = request.type,
                    title = details?.displayTitle ?: "",
                    request = request,
                )

                if (request.type == RequestType.Tv && request.seasons.isNotEmpty()) {
                    RequestCardSeasonInfo(seasons = request.seasons)
                }

                RequestButtons(
                    isAdmin = user?.hasPermission(UserPermission.ADMIN) == true,
                    request = request,
                    operationsState = requestOperationsState,
                    onApproveClicked = onApproveClicked,
                    onDeclineClicked = onDeclineClicked,
                    onEditClicked = onEditClicked,
                    onDeleteClicked = onDeleteClicked,
                    onRemoveFromServiceClicked = onRemoveFromServiceClicked,
                    onViewRequestClicked = onViewRequestClicked,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RequestCardHeader(
    posterUrl: String?,
    year: String,
    requestType: RequestType,
    title: String,
    request: MediaRequest,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AsyncImage(
            model = rememberRemoteImageData(posterUrl),
            contentDescription = null,
            modifier =
                Modifier
                    .height(110.dp)
                    .aspectRatio(AspectRatio.Poster.ratio, true)
                    .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Fit,
        )

        Column(
            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 110.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (year.isNotBlank()) {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f),
                        )
                    }
                    MediaRequestTypeChip(text = requestType.name, requestType)
                }
                StatusChip(request)
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            RequestMetadata(request)
        }
    }
}

@Composable
private fun RequestMetadata(request: MediaRequest) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        UserInfoRow(
            label = mokoString(MR.strings.requested_by),
            displayName = request.requestedBy.displayName,
            avatar = request.requestedBy.avatar,
            textColor = Color.White.copy(alpha = 0.9f),
        )
        Text(
            text = request.createdAt.format("HH:mm, MMM d, yyyy"),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f),
        )

        request.modifiedBy?.let { modifiedBy ->
            Spacer(Modifier.height(4.dp))
            UserInfoRow(
                label = mokoString(MR.strings.modified_by),
                displayName = modifiedBy.displayName,
                avatar = modifiedBy.avatar,
                textColor = Color.White.copy(alpha = 0.9f),
            )
            Text(
                text = request.updatedAt.format("HH:mm, MMM d, yyyy"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun RequestCardSeasonInfo(seasons: List<RequestSeason>) {
    Text(
        text = mokoString(MR.strings.seasons_header),
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.8f),
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(top = 2.dp),
    ) {
        seasons.forEach {
            Badge(
                containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                contentColor = MaterialTheme.colorScheme.surfaceVariant,
            ) { Text(it.seasonNumber.toString()) }
        }
    }
}
