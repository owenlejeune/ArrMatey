package com.dnfapps.arrmatey.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.dnfapps.arrmatey.MainActivity
import com.dnfapps.arrmatey.R
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import kotlinx.coroutines.flow.first

class AppShortcutManager(
    private val context: Context,
    private val moko: MokoStrings,
    private val instanceRepository: InstanceRepository
) {
    companion object {
        const val ACTION_OPEN_LIBRARY = "com.dnfapps.arrmatey.ACTION_OPEN_LIBRARY"
        const val ACTION_OPEN_SEARCH = "com.dnfapps.arrmatey.ACTION_OPEN_SEARCH"
        const val ACTION_OPEN_DOWNLOADS = "com.dnfapps.arrmatey.ACTION_OPEN_DOWNLOADS"
        const val ACTION_OPEN_ACTIVITY = "com.dnfapps.arrmatey.ACTION_OPEN_ACTIVITY"
        const val ACTION_OPEN_SCHEDULE = "com.dnfapps.arrmatey.ACTION_OPEN_SCHEDULE"
        const val ACTION_OPEN_REQUESTS = "com.dnfapps.arrmatey.ACTION_OPEN_REQUESTS"
        const val ACTION_OPEN_ISSUES = "com.dnfapps.arrmatey.ACTION_OPEN_ISSUES"

        const val EXTRA_INSTANCE_TYPE = "instance_type"
    }

    suspend fun updateShortcuts() {
        val shortcuts = mutableListOf<ShortcutInfoCompat>()
        val allInstances = instanceRepository.observeAllInstances().first()

        InstanceType.arrs().forEach { type ->
            if (allInstances.any { it.type == type }) {
                shortcuts.add(createShortcut(
                    id = "search_${type.name.lowercase()}",
                    label = "Search ${type.name}",
                    action = ACTION_OPEN_SEARCH,
                    iconRes = R.drawable.baseline_search_24,
                    extras = mapOf(EXTRA_INSTANCE_TYPE to type.name)
                ))

                shortcuts.add(createShortcut(
                    id = "library_${type.name.lowercase()}",
                    label = "${type.name} Library",
                    action = ACTION_OPEN_LIBRARY,
                    iconRes = R.drawable.outline_browse_24,
                    extras = mapOf(EXTRA_INSTANCE_TYPE to type.name)
                ))
            }
        }

        shortcuts.add(createShortcut(
            id = "downloads",
            label = moko.getString(MR.strings.downloads),
            action = ACTION_OPEN_DOWNLOADS,
            iconRes = R.drawable.outline_cloud_download_24
        ))

        if (allInstances.any { it.type == InstanceType.Seerr }) {
            shortcuts.add(createShortcut(
                id = "requests",
                label = moko.getString(MR.strings.requests),
                action = ACTION_OPEN_REQUESTS,
                iconRes = R.drawable.outline_inbox_24
            ))

//            shortcuts.add(createShortcut(
//                id = "issues",
//                label = moko.getString(MR.strings.issues),
//                action = ACTION_OPEN_ISSUES,
//                iconRes = R.drawable.outline_warning_24
//            ))
        }

        shortcuts.add(createShortcut(
            id = "calendar",
            label = moko.getString(MR.strings.calendar),
            action = ACTION_OPEN_SCHEDULE,
            iconRes = R.drawable.outline_calendar_today_24
        ))

        shortcuts.add(createShortcut(
            id = "activity",
            label = moko.getString(MR.strings.activity),
            action = ACTION_OPEN_ACTIVITY,
            iconRes = R.drawable.outline_download_24
        ))

        ShortcutManagerCompat.addDynamicShortcuts(context, shortcuts.shuffled())
    }

    private fun createShortcut(
        id: String,
        label: String,
        action: String,
        iconRes: Int,
        extras: Map<String, String> = emptyMap()
    ): ShortcutInfoCompat {
        val intent = Intent(context, MainActivity::class.java).apply {
            this.action = action
            extras.forEach { (key, value) -> putExtra(key, value) }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return ShortcutInfoCompat.Builder(context, id)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(IconCompat.createWithResource(context, iconRes))
            .setIntent(intent)
            .build()
    }
}
