package com.dnfapps.arrmatey.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ComicIssue
import com.dnfapps.arrmatey.arr.api.model.ComicVolume
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun ComicIssuesArea(
    volume: ComicVolume,
    searchIds: Set<Long>,
    modifier: Modifier = Modifier,
    onToggleMonitor: (ComicIssue) -> Unit = {},
    onAutomaticSearch: (Long) -> Unit = {},
    onVolumeAutomaticSearch: () -> Unit = {},
    onNavigateToComicRelease: (Long) -> Unit = {},
    onNavigateToVolumeRelease: () -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        val volumeId = volume.id ?: 0L
        val volumeSearchInProgress = searchIds.contains(volumeId)

        ReleaseDownloadButtons(
            onInteractiveClicked = onNavigateToVolumeRelease,
            onAutomaticClicked = onVolumeAutomaticSearch,
            automaticSearchEnabled = volume.monitored,
            automaticSearchInProgress = volumeSearchInProgress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
        )

        if (volume.issues.isEmpty()) {
            Text(
                text = mokoString(MR.strings.no_issues),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            volume.issues.forEach { issue ->
                val issueId = issue.id ?: 0L
                ComicIssueRow(
                    issue = issue,
                    searchInProgress = searchIds.contains(issueId),
                    onToggleMonitor = { onToggleMonitor(issue) },
                    onAutomaticSearch = { onAutomaticSearch(issueId) },
                    onNavigateToComicRelease = { onNavigateToComicRelease(issueId) },
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun ComicIssueRow(
    issue: ComicIssue,
    searchInProgress: Boolean,
    onToggleMonitor: () -> Unit,
    onAutomaticSearch: () -> Unit,
    onNavigateToComicRelease: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val isDownloaded = issue.files.isNotEmpty()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val titleString = buildAnnotatedString {
                val issueNum = issue.issueNumber ?: issue.calculatedIssueNumber?.toInt()?.toString()
                if (!issueNum.isNullOrBlank()) {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("#$issueNum ")
                    }
                }
                val issueTitle = issue.title
                if (!issueTitle.isNullOrBlank()) {
                    append(issueTitle)
                }
            }

            Text(
                text = titleString,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            val (statusText, statusColor) = when {
                isDownloaded -> mokoString(MR.strings.downloaded) to MaterialTheme.colorScheme.tertiary
                else -> mokoString(MR.strings.missing) to MaterialTheme.colorScheme.error
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontStyle = FontStyle.Italic,
                )

                issue.date?.let { releaseDate ->
                    Text(
                        text = "$BULLET$releaseDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        IconButton(
            onClick = onNavigateToComicRelease,
            modifier = Modifier.size(24.dp),
            enabled = issue.monitored,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
            )
        }

        IconButton(
            onClick = onAutomaticSearch,
            enabled = issue.monitored && !searchInProgress,
            modifier = Modifier.size(24.dp),
        ) {
            if (searchInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                )
            }
        }

        IconButton(
            onClick = onToggleMonitor,
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = if (issue.monitored) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = null,
            )
        }
    }
}
