package com.dnfapps.arrmatey.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
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
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.compose.utils.breakable
import com.dnfapps.arrmatey.entensions.Bullet
import com.dnfapps.arrmatey.extensions.isEqual
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.PosterItem
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.surfaceContainerLowDark
import com.dnfapps.arrmatey.ui.theme.surfaceDark
import com.dnfapps.arrmatey.ui.theme.surfaceVariantDark
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun MovieCalendarItem(
    date: LocalDate,
    movie: ArrMovie
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = ArrOrange,
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
            PosterItem(movie, Modifier.width(50.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title?.breakable() ?: mokoString(MR.strings.unknown),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (movie.inCinemas?.isEqual(date) == true) {
                        Text(
                            text = mokoString(MR.strings.in_cinemas),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (movie.digitalRelease?.isEqual(date) == true) {
                        Text(
                            text = mokoString(MR.strings.digital_release),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (movie.physicalRelease?.isEqual(date) == true) {
                        Text(
                            text = mokoString(MR.strings.physical_release),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text(
                    text = listOfNotNull(movie.certification, movie.studio)
                        .joinToString(Bullet),
                    style = MaterialTheme.typography.bodySmall,
                    color = surfaceContainerLowDark
                )
            }

            val statusIcon = when {
                movie.isDownloaded -> Icons.Default.FileDownloadDone
                !movie.monitored -> Icons.Default.BookmarkBorder
                movie.isWaiting -> Icons.Default.AccessTimeFilled
                movie.monitored -> Icons.Default.Bookmark
                else -> null
            }
            statusIcon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = surfaceContainerLowDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}