package com.dnfapps.arrmatey.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun CalendarDaySection(
    date: LocalDate,
    items: List<CalendarItem>,
    instances: List<Instance>,
    onItemClick: (CalendarItem, Long?) -> Unit,
) {
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val isToday = date == today
    val relativeDate = when {
        isToday -> mokoString(MR.strings.today)
        date == today.plus(1, DateTimeUnit.DAY) -> mokoString(MR.strings.tomorrow)
        date == today.minus(1, DateTimeUnit.DAY) -> mokoString(MR.strings.yesterday)
        else -> null
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = relativeDate ?: date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${date.month.name.take(3)} ${date.day}, ${date.year}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (items.isNotEmpty()) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = items.size.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        items.forEach { item ->
            val onNavigate: (Long?) -> Unit = { instanceId ->
                onItemClick(item, instanceId)
            }

            when (item) {
                is ArrMovie -> MovieCalendarItem(
                    date = date,
                    movie = item,
                    instances = instances,
                    onNavigate = onNavigate
                )

                is EpisodeGroup -> EpisodeCalendarItem(
                    episode = item.first,
                    additional = item.additional,
                    instances = instances,
                    onNavigate = onNavigate
                )

                is Episode -> EpisodeCalendarItem(
                    episode = item,
                    instances = instances,
                    onNavigate = onNavigate
                )

                is ArrAlbum -> AlbumCalendarItem(
                    album = item,
                    instances = instances,
                    onNavigate = onNavigate
                )

                is Book -> BookCalendarItem(
                    book = item,
                    instances = instances,
                    onNavigate = onNavigate
                )

                is Audiobook -> AudiobookCalendarItem(
                    audiobook = item,
                    instances = instances,
                    onNavigate = onNavigate
                )
            }
        }
    }
}