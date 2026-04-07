package com.dnfapps.arrmatey.notifications

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

class ScheduleNotificationUseCase(
    private val notificationManager: NotificationManager,
    private val mokoStrings: MokoStrings
) {
    operator fun invoke(
        instance: Instance,
        message: String,
        scheduledTime: Instant,
        notificationId: Int
    ) {
        if (instance.type.supportsNotifications && instance.notificationsEnabled) {
            val timeZone = TimeZone.currentSystemDefault()
            val localDateTime = scheduledTime.toLocalDateTime(timeZone)
            val morningTime = LocalDateTime(
                year = localDateTime.year,
                month = localDateTime.month,
                day = localDateTime.day,
                hour = 9,
                minute = 0,
                second = 0,
                nanosecond = 0
            )
            val finalScheduledTime = morningTime.toInstant(timeZone)

            if (finalScheduledTime > Clock.System.now()) {
                notificationManager.scheduleNotification(
                    id = notificationId,
                    title = mokoStrings.getString(MR.strings.new_release),
                    message = message,
                    scheduledTime = finalScheduledTime,
                    instanceName = instance.label
                )
            }
        }
    }
}
