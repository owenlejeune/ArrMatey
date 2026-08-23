package com.dnfapps.arrmatey.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.navigation.ArrScreen
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.AlbumCover
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.surfaceContainerLowDark
import com.dnfapps.arrmatey.ui.theme.surfaceDark
import com.dnfapps.arrmatey.utils.mokoString
import org.koin.compose.koinInject
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun AlbumCalendarItem(
    album: ArrAlbum
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = ArrGreen,
            contentColor = surfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumCover(album, Modifier.size(50.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.title ?: mokoString(MR.strings.unknown),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = album.artist?.title ?: mokoString(MR.strings.unknown),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            val statusIcon = when {
                album.isDownloaded -> Icons.Default.FileDownloadDone
                album.isPartiallyDownloaded -> Icons.Default.Download
                !album.monitored -> Icons.Default.BookmarkBorder
                album.monitored -> Icons.Default.Bookmark
                else -> null
            }
            statusIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = surfaceContainerLowDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}