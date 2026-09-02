package com.dnfapps.arrmatey.notifications

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class ScheduleNotificationUseCase(
    private val notificationManager: NotificationManager,
    private val mokoStrings: MokoStrings,
) {
    operator fun invoke(
        instance: Instance,
        item: CalendarItem,
    ) {
        if (instance.type.supportsNotifications && instance.notificationsEnabled) {
            val scheduledTime = item.notificationScheduledTime ?: return
            val timeZone = TimeZone.currentSystemDefault()
            val localDateTime = scheduledTime.toLocalDateTime(timeZone)
            val morningTime =
                LocalDateTime(
                    year = localDateTime.year,
                    month = localDateTime.month,
                    day = localDateTime.day,
                    hour = 9,
                    minute = 0,
                    second = 0,
                    nanosecond = 0,
                )
            val finalScheduledTime = morningTime.toInstant(timeZone)

            val title =
                when (instance.type) {
                    InstanceType.Sonarr -> MR.strings.new_episode
                    InstanceType.Radarr -> item.notificationReleaseType ?: MR.strings.new_release
                    InstanceType.Lidarr -> MR.strings.new_album
                    InstanceType.Bookshelf -> MR.strings.new_book
                    else -> MR.strings.new_release
                }

            if (finalScheduledTime > Clock.System.now()) {
                val extras = mutableMapOf<String, String>()
                extras[NotificationConstants.EXTRA_INSTANCE_TYPE] = instance.type.name
                extras[NotificationConstants.EXTRA_INSTANCE_ID] = instance.id.toString()

                when (item) {
                    is ArrMovie -> {
                        extras[NotificationConstants.EXTRA_ITEM_ID] = item.id.toString()
                        item.tmdbId?.let { extras[NotificationConstants.EXTRA_TMDB_ID] = it.toString() }
                    }

                    is Episode -> {
                        extras[NotificationConstants.EXTRA_ITEM_ID] = item.seriesId.toString()
                        extras[NotificationConstants.EXTRA_EPISODE_ID] = item.id.toString()
                        item.series?.tmdbId?.let { extras[NotificationConstants.EXTRA_TMDB_ID] = it.toString() }
                    }

                    is ArrAlbum -> {
                        extras[NotificationConstants.EXTRA_ITEM_ID] = item.id.toString()
                    }

                    is Book -> {
                        extras[NotificationConstants.EXTRA_ITEM_ID] = item.authorId.toString()
                        extras[NotificationConstants.EXTRA_BOOK_ID] = item.id.toString()
                    }

                    is Audiobook -> {
                        extras[NotificationConstants.EXTRA_ITEM_ID] = item.id.toString()
                    }

                    else -> {}
                }

                notificationManager.scheduleNotification(
                    id = item.calendarId.toInt(),
                    title = mokoStrings.getString(title),
                    message = item.notificationMessage,
                    scheduledTime = finalScheduledTime,
                    instanceName = instance.label,
                    extras = extras,
                )
            }
        }
    }
}
