package com.dnfapps.arrmatey.notifications

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import dev.icerock.moko.resources.StringResource
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ScheduleNotificationUseCase(
    private val notificationManager: NotificationManager,
    private val mokoStrings: MokoStrings
) {
    operator fun invoke(
        instance: Instance,
        message: String,
        scheduledTime: Instant,
        notificationId: Int,
        releaseType: StringResource? = null
    ) {
        println("can show notifications - ${instance.type.supportsNotifications} && ${instance.notificationsEnabled}")
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

            val title = when (instance.type) {
                InstanceType.Sonarr -> MR.strings.new_episode
                InstanceType.Radarr -> releaseType ?: MR.strings.new_release
                InstanceType.Lidarr -> MR.strings.new_album
                else -> MR.strings.new_release
            }

            println("about to schedule notification")
            if (finalScheduledTime > Clock.System.now()) {
                notificationManager.scheduleNotification(
                    id = notificationId,
                    title = mokoStrings.getString(title),
                    message = message,
                    scheduledTime = Clock.System.now().plus(15.seconds),//finalScheduledTime,
                    instanceName = instance.label
                )
            }
        }
    }
}
