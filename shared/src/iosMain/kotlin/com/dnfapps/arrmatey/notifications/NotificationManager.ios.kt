package com.dnfapps.arrmatey.notifications

import dev.shivathapaa.logger.api.Logger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.time.Clock
import kotlin.time.Instant

actual class NotificationManager(
    private val logger: Logger,
) {
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    actual fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        scheduledTime: Instant,
        instanceName: String,
        extras: Map<String, String>,
    ) {
        val content =
            UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(message)
                setUserInfo(
                    extras +
                        mapOf(
                            "instanceName" to instanceName,
                            "action" to NotificationConstants.ACTION_OPEN_SCHEDULE,
                        ),
                )
            }

        val now = Clock.System.now()
        val timeInterval = (scheduledTime - now).inWholeSeconds.toDouble()

        logger.info { "iOS notification time interval - $timeInterval" }

        if (timeInterval <= 0) return

        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(timeInterval, false)
        val request = UNNotificationRequest.requestWithIdentifier(id.toString(), content, trigger)

        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {
                logger.error { "Error scheduling notification: ${error.localizedDescription}" }
            } else {
                logger.info { "Notification set successfully" }
            }
        }
    }

    actual fun cancelNotification(id: Int) {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(id.toString()))
    }

    actual fun cancelAllNotifications() {
        notificationCenter.removeAllPendingNotificationRequests()
    }

    actual fun cancelNotificationsForInstance(instanceName: String) {
        notificationCenter.getPendingNotificationRequestsWithCompletionHandler { requests ->
            val identifiersToCancel =
                requests
                    ?.filterIsInstance<UNNotificationRequest>()
                    ?.filter { it.content.userInfo["instanceName"] == instanceName }
                    ?.map { it.identifier }

            if (!identifiersToCancel.isNullOrEmpty()) {
                notificationCenter.removePendingNotificationRequestsWithIdentifiers(identifiersToCancel)
            }
        }
    }

    actual fun showProgressNotification(
        id: Int,
        title: String,
        message: String,
        progress: Float,
        instanceName: String,
    ) {
        if (progress >= 1f) {
            showNotification(id, title, message, instanceName)
        } else {
            val content =
                UNMutableNotificationContent().apply {
                    setTitle(title)
                    setBody("$message (${(progress * 100).toInt()}%)")
                    setUserInfo(mapOf("action" to NotificationConstants.ACTION_OPEN_SCHEDULE))
                }
            val request = UNNotificationRequest.requestWithIdentifier(id.toString(), content, null)
            notificationCenter.addNotificationRequest(request, null)
        }
    }

    actual fun showNotification(
        id: Int,
        title: String,
        message: String,
        instanceName: String,
    ) {
        val content =
            UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(message)
                setUserInfo(mapOf("action" to NotificationConstants.ACTION_OPEN_SCHEDULE))
            }
        val request = UNNotificationRequest.requestWithIdentifier(id.toString(), content, null)
        notificationCenter.addNotificationRequest(request, null)
    }
}
