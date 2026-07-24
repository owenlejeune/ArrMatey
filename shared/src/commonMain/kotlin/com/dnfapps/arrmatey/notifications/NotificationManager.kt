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

    fun showProgressNotification(
        id: Int,
        title: String,
        message: String,
        progress: Float,
        instanceName: String
    )

    fun showNotification(
        id: Int,
        title: String,
        message: String,
        instanceName: String
    )
}
