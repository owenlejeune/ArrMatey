package com.dnfapps.arrmatey.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.extensions.localToday
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrLightPurple
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrRed
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun CalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    items: List<CalendarItem>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { Clock.localToday() }
    val isToday = date == today

    Surface(
        modifier =
            modifier
                .aspectRatio(1f)
                .padding(2.dp),
        onClick = onClick,
        color =
            when {
                isSelected -> MaterialTheme.colorScheme.inversePrimary
                isToday -> MaterialTheme.colorScheme.surfaceContainerHigh
                else -> Color.Transparent
            },
        shape = MaterialTheme.shapes.medium,
        border =
            when {
                isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                isToday -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                else -> null
            },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Day Number Pill / Capsule
            Box(
                modifier =
                    Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> Color.Transparent
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = date.day.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color =
                        when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (items.isNotEmpty()) {
                val movieCount = items.count { it is ArrMovie }
                val episodeCount = items.count { it is Episode || it is EpisodeGroup }
                val albumCount = items.count { it is ArrAlbum }
                val bookCount = items.count { it is Book }
                val audiobooksCount = items.count { it is Audiobook }

                val activeMediaTypes =
                    buildList {
                        if (episodeCount > 0) add(ArrBlue)
                        if (movieCount > 0) add(ArrOrange)
                        if (albumCount > 0) add(ArrGreen)
                        if (bookCount > 0) add(ArrRed)
                        if (audiobooksCount > 0) add(ArrLightPurple)
                    }

                // Multi-colored media indicator strips / capsules
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    activeMediaTypes.take(5).forEach { color ->
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(3.5.dp)
                                    .clip(CircleShape)
                                    .background(color),
                        )
                    }
                }
            }
        }
    }
}
