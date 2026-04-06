package com.dnfapps.arrmatey.notifications

import kotlin.time.Instant

expect class NotificationManager {
    fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        scheduledTime: Instant,
        instanceName: String
    )

    fun cancelNotification(id: Int)
    fun cancelAllNotifications()
    fun cancelNotificationsForInstance(instanceName: String)
}
