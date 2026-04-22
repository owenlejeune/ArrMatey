package com.dnfapps.arrmatey.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import kotlin.time.Instant

actual class NotificationManager(
    private val context: Context,
    private val mokoStrings: MokoStrings
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

    private fun getChannelId(instanceName: String) = "release_notifications_$instanceName"

    private fun ensureChannelExists(instanceName: String): String {
        val channelId = getChannelId(instanceName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = mokoStrings.getString(MR.strings.instance_notification_channel, listOf(instanceName))
            val channel = NotificationChannel(
                channelId,
                channelName,
                AndroidNotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }

    actual fun scheduleNotification(
        id: Int,
        title: String,
        message: String,
        scheduledTime: Instant,
        instanceName: String
    ) {
        val channelId = ensureChannelExists(instanceName)
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("message", message)
            putExtra("channelId", channelId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            scheduledTime.toEpochMilliseconds(),
            pendingIntent
        )
    }

    actual fun cancelNotification(id: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        notificationManager.cancel(id)
    }

    actual fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    actual fun cancelNotificationsForInstance(instanceName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getChannelId(instanceName)
            notificationManager.deleteNotificationChannel(channelId)
        }
        notificationManager.cancelAll()
    }
}
