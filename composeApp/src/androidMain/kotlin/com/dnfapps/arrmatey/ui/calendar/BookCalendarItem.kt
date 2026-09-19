package com.dnfapps.arrmatey.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.entensions.BULLET
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.ui.components.BasePosterItem
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.ArrRed
import com.dnfapps.arrmatey.ui.theme.surfaceContainerLowDark
import com.dnfapps.arrmatey.ui.theme.surfaceDark

@Composable
fun BookCalendarItem(
    book: Book,
    instances: List<Instance>,
    useFullColorCards: Boolean = false,
    onNavigate: (Long?) -> Unit,
) {
    val associatedColor = book.associatedType?.associatedColor ?: ArrRed
    val containerColor =
        if (useFullColorCards) {
            associatedColor
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    val contentColor =
        if (useFullColorCards) {
            surfaceDark
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    val secondaryContentColor =
        if (useFullColorCards) {
            surfaceContainerLowDark
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    SlidableCalendarItem(
        instanceIds = book.instanceIds,
        instances = instances,
        onInstanceSelected = onNavigate,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            shape = MaterialTheme.shapes.large,
            colors =
                CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!useFullColorCards) {
                    Box(
                        modifier =
                            Modifier
                                .width(6.dp)
                                .fillMaxHeight()
                                .background(associatedColor),
                    )
                }

                Row(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasePosterItem(
                        model = rememberRemoteImageData(book.getCover()?.remoteUrl),
                        modifier = Modifier.width(50.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor,
                        )
                        Text(
                            text =
                                listOfNotNull(
                                    book.authorTitle,
                                    book.seriesTitle?.takeUnless { it.isEmpty() },
                                ).joinToString(separator = BULLET),
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondaryContentColor,
                        )
                    }

                    val statusIcon =
                        when {
                            book.isDownloaded -> Icons.Default.FileDownloadDone
                            book.isPartiallyDownloaded -> Icons.Default.Download
                            !book.monitored -> Icons.Default.BookmarkBorder
                            book.monitored -> Icons.Default.Bookmark
                            else -> null
                        }
                    statusIcon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = secondaryContentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
