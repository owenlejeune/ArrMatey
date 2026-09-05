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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
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
import com.dnfapps.arrmatey.seerr.api.model.IssueType
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.BannerView
import com.dnfapps.arrmatey.ui.components.MediaRequestTypeChip
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.screens.requests.IssueStatusChip
import com.dnfapps.arrmatey.ui.screens.requests.UserInfoRow
import com.dnfapps.arrmatey.ui.theme.TranslucentBlack
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun DashboardPendingIssuesSection(
    state: CombinedDashboardState.Success,
    enabled: Boolean = true,
    onIssueClick: (MediaIssuePackage) -> Unit = {},
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
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = mokoString(MR.strings.issues),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            val openIssues = state.openIssues
            if (openIssues.isEmpty()) {
                Text(
                    text = mokoString(MR.strings.no_issues_found),
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
                    items(openIssues, key = { it.issue.id }) { item ->
                        CompactIssueCard(
                            issuePackage = item,
                            onClick = { if (enabled) onIssueClick(item) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CompactIssueCard(
    issuePackage: MediaIssuePackage,
    onClick: () -> Unit,
) {
    val issue = issuePackage.issue
    val details = issuePackage.details
    val issueType = IssueType.fromValue(issue.issueType)

    Card(
        modifier =
            Modifier
                .width(260.dp)
                .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            BannerView(
                bannerModel = details?.fullPosterPath?.let { rememberRemoteImageData(it) },
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
                                .height(72.dp)
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
                            issue.media?.mediaType?.let { requestType ->
                                MediaRequestTypeChip(text = requestType.name, requestType)
                            }
                        }

                        Text(
                            text = details?.displayTitle ?: mokoString(MR.strings.unknown),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            IssueStatusChip(issue)
                            Text(
                                text = mokoString(issueType.label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (issue.media?.mediaType == RequestType.Tv) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val seasonLabel = issue.problemSeason.takeUnless { it == 0 }?.toString() ?: mokoString(MR.strings.all)
                        val episodeLabel = issue.problemEpisode.takeUnless { it == 0 }?.toString() ?: mokoString(MR.strings.all)
                        Text(
                            text = "S$seasonLabel E$episodeLabel",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                issue.createdBy?.let { createdBy ->
                    UserInfoRow(
                        label = mokoString(MR.strings.opened_by),
                        displayName = createdBy.displayName,
                        avatar = createdBy.avatar,
                    )
                }
            }
        }
    }
}
