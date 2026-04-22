package com.dnfapps.arrmatey.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.dnfapps.arrmatey.shared.MR

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "New Release"
        val message = intent.getStringExtra("message") ?: ""
        val channelId = intent.getStringExtra("channelId") ?: ""

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(MR.images.icon.drawableResId)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(id, builder.build())
    }
}