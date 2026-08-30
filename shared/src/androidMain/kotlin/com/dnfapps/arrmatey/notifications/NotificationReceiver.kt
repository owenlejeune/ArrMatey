package com.dnfapps.arrmatey.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.dnfapps.arrmatey.shared.MR

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val id = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "New Release"
        val message = intent.getStringExtra("message") ?: ""
        val channelId = intent.getStringExtra("channelId") ?: ""

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        val contentIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                action = NotificationConstants.ACTION_OPEN_SCHEDULE
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                intent.extras?.let { putExtras(it) }
            }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                id,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val builder =
            NotificationCompat
                .Builder(context, channelId)
                .setSmallIcon(MR.images.icon.drawableResId)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        notificationManager.notify(id, builder.build())
    }
}
