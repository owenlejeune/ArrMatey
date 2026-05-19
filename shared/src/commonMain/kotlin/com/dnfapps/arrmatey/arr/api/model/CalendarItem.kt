package com.dnfapps.arrmatey.arr.api.model

import dev.icerock.moko.resources.StringResource
import kotlin.time.Instant

sealed interface CalendarItem {
    val instanceId: Long?
    val calendarId: Long
    fun getCalendarDates(): List<Instant>
    val notificationScheduledTime: Instant?
    val notificationMessage: String
    val notificationReleaseType: StringResource? get() = null
}
