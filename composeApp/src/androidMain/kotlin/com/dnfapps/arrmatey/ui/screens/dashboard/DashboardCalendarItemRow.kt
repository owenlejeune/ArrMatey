package com.dnfapps.arrmatey.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.arr.api.model.InstanceTypeIdentifiable
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.format
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun DashboardCalendarItemRow(
    item: CalendarItem,
    showDate: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val title = when (item) {
            is Episode -> item.series?.title ?: ""
            is EpisodeGroup -> item.first.series?.title ?: ""
            is ArrAlbum -> item.artist?.title ?: ""
            is ArrMovie -> item.title ?: ""
            is Audiobook -> item.title ?: ""
            is Book -> item.title
        }
        val sub = when (item) {
            is Episode -> "${item.seasonEpLabel}: ${item.title ?: ""}"
            is EpisodeGroup -> {
                val episodes = listOf(item.first) + item.additional
                episodes.joinToString(", ") { "${it.seasonEpLabel}: ${it.title ?: ""}" }
            }
            is ArrAlbum -> item.title ?: ""
            is ArrMovie -> {
                val date = item.releaseDate ?: item.digitalRelease ?: item.physicalRelease ?: item.inCinemas
                val label = when (date) {
                    item.physicalRelease -> mokoString(MR.strings.physical_release)
                    item.digitalRelease -> mokoString(MR.strings.digital_release)
                    item.inCinemas -> mokoString(MR.strings.in_cinemas)
                    else -> mokoString(MR.strings.release_date)
                }
                label
            }
            else -> ""
        }

        val color = item.associatedType?.associatedColor
            ?: MaterialTheme.colorScheme.primary

        Box(Modifier.size(4.dp).clip(CircleShape).background(color))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            if (sub.isNotBlank()) {
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (showDate) {
                val firstDate = item.getCalendarDates().firstOrNull()
                firstDate?.let {
                    val date = it.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    Text(
                        text = date.format("EEE, MMM d"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}