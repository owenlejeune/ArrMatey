package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
import com.dnfapps.arrmatey.ui.theme.inverseOnSurfaceLight
import com.dnfapps.arrmatey.ui.theme.inverseSurfaceLight
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
) {
    val request = mediaPackage.request
    val details = mediaPackage.details

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = inverseSurfaceLight,
            contentColor = inverseOnSurfaceLight
        ),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            BannerView(
                bannerModel = details?.fullBackdropPath?.let { rememberRemoteImageData(it) },
                modifier = Modifier.matchParentSize()
            )
            Box(modifier = Modifier.matchParentSize().background(TranslucentBlack))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(18.dp).fillMaxWidth()
            ) {
                RequestCardHeader(
                    posterUrl = details?.fullPosterPath,
                    year = details?.displayDate?.year?.toString() ?: "",
                    requestType = request.type,
                    title = details?.displayTitle ?: "",
                    request = request
                )

                if (request.type == RequestType.Tv && request.seasons.isNotEmpty()) {
                    RequestCardSeasonInfo(seasons = request.seasons)
                }

                Spacer(Modifier.height(12.dp))

                RequestButtons(
                    isAdmin = user?.hasPermission(UserPermission.ADMIN) == true,
                    request = request,
                    operationsState = requestOperationsState,
                    onApproveClicked = onApproveClicked,
                    onDeclineClicked = onDeclineClicked,
                    onEditClicked = onEditClicked,
                    onDeleteClicked = onDeleteClicked,
                    onRemoveFromServiceClicked = onRemoveFromServiceClicked
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
    request: MediaRequest
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = rememberRemoteImageData(posterUrl),
            contentDescription = null,
            modifier = Modifier
                .height(100.dp)
                .aspectRatio(AspectRatio.Poster.ratio, true)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )

        Column(modifier = Modifier.defaultMinSize(minHeight = 100.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = year,
                    style = MaterialTheme.typography.labelMedium
                )
                MediaRequestTypeChip(text = requestType.name, requestType)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLargeEmphasized,
                modifier = Modifier.padding(top = 2.dp)
            )
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusChip(request)
                RequestMetadata(request)
            }
        }
    }
}

@Composable
private fun RequestMetadata(request: MediaRequest) {
    Column {
        UserInfoRow(
            label = mokoString(MR.strings.requested_by),
            displayName = request.requestedBy.displayName,
            avatar = request.requestedBy.avatar
        )
        Text(
            text = request.createdAt.format("HH:mm, MMM d, yyyy"),
            style = MaterialTheme.typography.bodySmall
        )

        request.modifiedBy?.let { modifiedBy ->
            Spacer(Modifier.height(6.dp))
            UserInfoRow(
                label = mokoString(MR.strings.modified_by),
                displayName = modifiedBy.displayName,
                avatar = modifiedBy.avatar
            )
            Text(
                text = request.updatedAt.format("HH:mm, MMM d, yyyy"),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RequestCardSeasonInfo(seasons: List<RequestSeason>) {
    Text(
        text = mokoString(MR.strings.seasons_header),
        style = MaterialTheme.typography.labelSmall
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.padding(top = 2.dp)
    ) {
        seasons.forEach {
            Badge(
                containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                contentColor = MaterialTheme.colorScheme.surfaceVariant
            ) { Text(it.seasonNumber.toString()) }
        }
    }
}