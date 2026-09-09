package com.dnfapps.arrmatey.entensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

fun Context.openLink(url: String) {
    val uri = url.toUri()
    try {
        val customTabIntent =
            CustomTabsIntent
                .Builder()
                .setShowTitle(true)
                .setInstantAppsEnabled(true)
                .build()
        customTabIntent.launchUrl(this, uri)
    } catch (e: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}

fun Context.openAppSettings() {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    startActivity(intent)
}
