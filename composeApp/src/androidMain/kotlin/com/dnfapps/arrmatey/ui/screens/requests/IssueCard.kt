package com.dnfapps.arrmatey.ui.screens.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnfapps.arrmatey.seerr.api.model.Issue
import com.dnfapps.arrmatey.seerr.api.model.IssueType
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.seerr.api.model.RequestType
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

@Composable
fun IssueCard(
    issuePackage: MediaIssuePackage,
    onClick: () -> Unit
) {
    val issue = issuePackage.issue
    val details = issuePackage.details

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
                bannerModel = details?.fullPosterPath?.let {
                    rememberRemoteImageData(it)
                },
                modifier = Modifier.matchParentSize()
            )
            Box(modifier = Modifier.matchParentSize().background(TranslucentBlack))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(18.dp).fillMaxWidth()
            ) {
                IssueCardHeader(
                    posterUrl = details?.fullPosterPath,
                    year = details?.displayDate?.year?.toString() ?: "",
                    requestType = issue.media?.mediaType,
                    title = details?.displayTitle ?: "",
                    issue = issue
                )

                if (issue.media?.mediaType == RequestType.Tv) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = mokoString(MR.strings.season),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = issue.problemSeason
                                    .takeUnless { it == 0 }?.toString()
                                    ?: mokoString(MR.strings.all),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text(
                            text = mokoString(MR.strings.episode),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = issue.problemEpisode
                                    .takeUnless { it == 0 }?.toString()
                                    ?: mokoString(MR.strings.all),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                issue.comments.minByOrNull { it.id }?.let { comment ->
                    Text(
                        text = comment.message,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IssueCardHeader(
    posterUrl: String?,
    year: String,
    requestType: RequestType?,
    title: String,
    issue: Issue
) {
    val issueType = IssueType.fromValue(issue.issueType)

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

        Column(
            modifier = Modifier.defaultMinSize(minHeight = 100.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.labelMedium
                        )
                        requestType?.let { requestType ->
                            MediaRequestTypeChip(text = requestType.name, requestType)
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                IssueStatusChip(issue)
            }
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(mokoString(MR.strings.type))
                    }
                    append(" ")
                    append(mokoString(issueType.label))
                },
                style = MaterialTheme.typography.bodyMedium
            )
            issue.createdBy?.let { createdBy ->
                Column {
                    UserInfoRow(
                        label = mokoString(MR.strings.opened_by),
                        displayName = createdBy.displayName,
                        avatar = createdBy.avatar
                    )
                    issue.createdAt?.let { createdAt ->
                        Text(
                            text = createdAt.format("HH:mm, MMM d, yyyy"),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}