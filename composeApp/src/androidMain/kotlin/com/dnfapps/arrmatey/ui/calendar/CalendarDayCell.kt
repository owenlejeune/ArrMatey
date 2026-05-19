package com.dnfapps.arrmatey.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    movieCount: Int,
    episodeCount: Int,
    albumCount: Int,
    bookCount: Int,
    audiobooksCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { Clock.localToday() }
    val isToday = date == today

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        onClick = onClick,
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        shape = RoundedCornerShape(8.dp),
        border = if (isToday && !isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.weight(1f))

            if ((movieCount + episodeCount + albumCount + bookCount + audiobooksCount) > 0) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (movieCount > 0) {
                        item {
                            GridBadge(movieCount, ArrOrange, Color.Black)
                        }
                    }
                    if (episodeCount > 0) {
                        item {
                            GridBadge(episodeCount, ArrBlue, Color.Black)
                        }
                    }
                    if (albumCount > 0) {
                        item {
                            GridBadge(albumCount, ArrGreen, Color.White)
                        }
                    }
                    if (bookCount > 0) {
                        item {
                            GridBadge(bookCount, ArrRed, Color.Black)
                        }
                    }
                    if (audiobooksCount > 0) {
                        item {
                            GridBadge(audiobooksCount, ArrLightPurple, Color.White)
                        }
                    }
                }
            }
        }
    }
}