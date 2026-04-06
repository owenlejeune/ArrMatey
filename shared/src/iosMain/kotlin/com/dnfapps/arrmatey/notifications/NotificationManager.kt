package com.dnfapps.arrmatey.notifications

import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationTrigger
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSinceDate
import kotlin.time.Instant
import kotlin.time.Clock

actual class NotificationManager {

    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    actual fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        scheduledTime: Instant,
        instanceName: String
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(message)
        }

        val now = Clock.System.now()
        val timeInterval = (scheduledTime - now).inWholeSeconds.toDouble()

        if (timeInterval <= 0) return

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(timeInterval, false)
        val request = UNNotificationRequest.requestWithIdentifier(id.toString(), content, trigger)

        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error scheduling notification: ${error.localizedDescription}")
            }
        }
    }

    actual fun cancelNotification(id: Int) {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(id.toString()))
    }

    actual fun cancelAllNotifications() {
        notificationCenter.removeAllPendingNotificationRequests()
    }
}
