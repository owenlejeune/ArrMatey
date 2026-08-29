package com.dnfapps.arrmatey.entensions

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun LocalDate.daysInMonth(): Int {
    val firstOfThisMonth = LocalDate(year, month, 1)
    val firstOfNextMonth = firstOfThisMonth.plus(1, DateTimeUnit.MONTH)
    val lastDay = firstOfNextMonth.minus(1, DateTimeUnit.DAY)
    return lastDay.day
}
