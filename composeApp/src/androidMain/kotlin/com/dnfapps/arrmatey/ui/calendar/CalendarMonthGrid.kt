package com.dnfapps.arrmatey.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.dnfapps.arrmatey.arr.state.CalendarState
import com.dnfapps.arrmatey.entensions.daysInMonth
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import kotlinx.datetime.LocalDate

@Composable
fun CalendarMonthGrid(
    currentMonth: LocalDate,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    state: CalendarState
) {
    val firstDayOfMonth = LocalDate(currentMonth.year, currentMonth.month, 1)
    val daysInMonth = currentMonth.daysInMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal+1

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(
                MR.strings.sun, MR.strings.mon, MR.strings.tues, MR.strings.wed,
                MR.strings.thu, MR.strings.fri, MR.strings.sat
            ).forEach { day ->
                Text(
                    text = mokoString(day),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val rows = (daysInMonth + firstDayOfWeek + 6) / 7
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayIndex = row * 7 + col - firstDayOfWeek

                    if (dayIndex in 0 until daysInMonth) {
                        val date = LocalDate(currentMonth.year, currentMonth.month, dayIndex + 1)

                        CalendarDayCell(
                            date = date,
                            isSelected = date == selectedDate,
                            items = state.items[date] ?: emptyList(),
                            modifier = Modifier.weight(1f),
                            onClick = { onDateSelected(date) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}