package com.dnfapps.arrmatey.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.arr.state.CalendarState
import com.dnfapps.arrmatey.extensions.localToday
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun CalendarMonthView(
    state: CalendarState,
    onLoadMore: () -> Unit
) {
    val today = remember { Clock.localToday() }
    var currentMonth by remember { mutableStateOf(today) }
    var selectedDate by remember { mutableStateOf(today) }

    val isCurrentMonth by remember(currentMonth) {
        derivedStateOf {
            currentMonth.month == today.month && currentMonth.year == today.year
        }
    }

    LaunchedEffect(currentMonth, state.dates) {
        if (selectedDate.month != currentMonth.month || selectedDate.year != currentMonth.year) {
             selectedDate = if (isCurrentMonth) today else LocalDate(
                 currentMonth.year,
                 currentMonth.month,
                 1
             )
        }

        val lastDayOfMonth = LocalDate(currentMonth.year, currentMonth.month, 1)
            .plus(1, DateTimeUnit.MONTH)
            .minus(1, DateTimeUnit.DAY)

        if (state.dates.isNotEmpty() && lastDayOfMonth > state.dates.last()) {
            onLoadMore()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentMonth = currentMonth.minus(1, DateTimeUnit.MONTH)
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
            }

            Text(
                text = "${currentMonth.month.name} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge,
                color = if (isCurrentMonth) MaterialTheme.colorScheme.primary else Color.Unspecified,
                modifier = Modifier.clickable {
                    if (!isCurrentMonth) currentMonth = today
                }
            )

            IconButton(onClick = {
                currentMonth = currentMonth.plus(1, DateTimeUnit.MONTH)
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        CalendarMonthGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it },
            state = state
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

        if (selectedDate.month == currentMonth.month && selectedDate.year == currentMonth.year) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    CalendarDaySection(
                        date = selectedDate,
                        items = state.items[selectedDate] ?: emptyList()
                    )
                }
            }
        }
    }
}