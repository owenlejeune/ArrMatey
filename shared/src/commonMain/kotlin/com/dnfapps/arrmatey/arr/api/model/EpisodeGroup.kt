package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class EpisodeGroup(
    val first: Episode,
    val additional: List<Episode>,
    val totalCount: Int = 1 + additional.size
): CalendarItem {

    override val instanceId: Long?
        get() = first.instanceId

    override val calendarId: Long
        get() = first.calendarId

    override fun getCalendarDates(): List<Instant> =
        first.getCalendarDates()

    override val notificationScheduledTime: Instant?
        get() = first.notificationScheduledTime

    override val notificationMessage: String
        get() = first.notificationMessage

}