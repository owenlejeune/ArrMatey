package com.dnfapps.arrmatey.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
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
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.BasePosterItem
import com.dnfapps.arrmatey.ui.helpers.rememberRemoteImageData
import com.dnfapps.arrmatey.ui.theme.ArrLightPurple
import com.dnfapps.arrmatey.utils.AspectRatio
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun AudiobookCalendarItem(
    audiobook: Audiobook
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = ArrLightPurple
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasePosterItem(
                model = rememberRemoteImageData(audiobook.getPoster()?.remoteUrl),
                aspectRatio = AspectRatio.Cover,
                modifier = Modifier.width(50.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audiobook.title ?: mokoString(MR.strings.unknown),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = listOfNotNull(
                        audiobook.authors.joinToString(", "),
                        audiobook.series?.takeUnless { it.isEmpty() }
                    ).joinToString(separator = Bullet),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            val statusIcon = when {
                audiobook.isDownloaded -> Icons.Default.FileDownloadDone
                !audiobook.monitored -> Icons.Default.BookmarkBorder
                audiobook.monitored -> Icons.Default.Bookmark
                else -> null
            }
            statusIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}