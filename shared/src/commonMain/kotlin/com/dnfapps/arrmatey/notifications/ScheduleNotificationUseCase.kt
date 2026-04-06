package com.dnfapps.arrmatey.notifications

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
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
            if (scheduledTime > Clock.System.now()) {
                notificationManager.scheduleNotification(
                    id = notificationId,
                    title = mokoStrings.getString(MR.strings.new_release),
                    message = message,
                    scheduledTime = Clock.System.now().plus(10, DateTimeUnit.SECOND),//scheduledTime,
                    instanceName = instance.label
                )
            }
        }
    }
}
