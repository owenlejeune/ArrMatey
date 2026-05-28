package com.dnfapps.arrmatey.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.dnfapps.arrmatey.MainActivity
import com.dnfapps.arrmatey.R
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.datastore.AndroidPreferencesStore
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientRepository
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.MokoStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AppShortcutManager(
    private val context: Context,
    private val moko: MokoStrings,
    private val instanceRepository: InstanceRepository,
    private val downloadClientRepository: DownloadClientRepository,
    private val preferenceStore: AndroidPreferencesStore
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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

    data class ShortcutItem(
        val id: String,
        val label: String,
        val action: String,
        val iconRes: Int,
        val extras: Map<String, String> = emptyMap()
    )

    init {
        combine(
            instanceRepository.observeAllInstances(),
            downloadClientRepository.observeAllDownloadClients(),
            preferenceStore.shortcutsOrder,
            preferenceStore.disabledShortcuts
        ) { _, _, _, _ -> }
            .onEach { updateShortcuts() }
            .launchIn(scope)
    }

    suspend fun getAllAvailableShortcuts(): List<ShortcutItem> {
        val shortcuts = mutableListOf<ShortcutItem>()
        val allInstances = instanceRepository.observeAllInstances().first()

        InstanceType.arrs().forEach { type ->
            if (allInstances.any { it.type == type }) {
                shortcuts.add(ShortcutItem(
                    id = "search_${type.name.lowercase()}",
                    label = moko.getString(MR.strings.arr_search_shortcut, listOf(type.name)),
                    action = ACTION_OPEN_SEARCH,
                    iconRes = R.drawable.baseline_search_24,
                    extras = mapOf(EXTRA_INSTANCE_TYPE to type.name)
                ))

                shortcuts.add(ShortcutItem(
                    id = "library_${type.name.lowercase()}",
                    label = moko.getString(MR.strings.arr_library_shortcut, listOf(type.name)),
                    action = ACTION_OPEN_LIBRARY,
                    iconRes = type.tabIcon?.drawableResId ?: R.drawable.outline_browse_24,
                    extras = mapOf(EXTRA_INSTANCE_TYPE to type.name)
                ))
            }
        }

        val allDownloadClients = downloadClientRepository.observeAllDownloadClients().first()
        if (allDownloadClients.isNotEmpty()) {
            shortcuts.add(ShortcutItem(
                id = "downloads",
                label = moko.getString(MR.strings.downloads),
                action = ACTION_OPEN_DOWNLOADS,
                iconRes = R.drawable.outline_cloud_download_24
            ))
        }

        if (allInstances.any { it.type == InstanceType.Seerr }) {
            shortcuts.add(ShortcutItem(
                id = "requests",
                label = moko.getString(MR.strings.requests),
                action = ACTION_OPEN_REQUESTS,
                iconRes = R.drawable.outline_inbox_24
            ))
        }

        shortcuts.add(ShortcutItem(
            id = "calendar",
            label = moko.getString(MR.strings.schedule),
            action = ACTION_OPEN_SCHEDULE,
            iconRes = R.drawable.outline_calendar_today_24
        ))

        shortcuts.add(ShortcutItem(
            id = "activity",
            label = moko.getString(MR.strings.activity),
            action = ACTION_OPEN_ACTIVITY,
            iconRes = R.drawable.outline_download_24
        ))

        return shortcuts
    }

    suspend fun updateShortcuts() {
        val availableShortcuts = getAllAvailableShortcuts()
        val order = preferenceStore.shortcutsOrder.first()
        val disabled = preferenceStore.disabledShortcuts.first()

        val shortcutMap = availableShortcuts.associateBy { it.id }
        
        val newShortcuts = availableShortcuts.filter { it.id !in order }
        
        val orderedShortcuts = if (order.isEmpty()) {
            availableShortcuts
        } else {
            val existingOrder = order.mapNotNull { shortcutMap[it] }
            // Find the index of the first disabled shortcut to insert new enabled ones before it
            val firstDisabledIndex = existingOrder.indexOfFirst { it.id in disabled }
            if (firstDisabledIndex == -1) {
                existingOrder + newShortcuts
            } else {
                val list = existingOrder.toMutableList()
                list.addAll(firstDisabledIndex, newShortcuts)
                list
            }
        }

        // If there are new shortcuts or some were removed from order, save the updated order
        val currentOrderIds = orderedShortcuts.map { it.id }
        if (order.isNotEmpty() && (newShortcuts.isNotEmpty() || order.size != currentOrderIds.size)) {
            preferenceStore.saveShortcutsOrder(currentOrderIds)
        }

        val shortcutsToDisplay = orderedShortcuts.filter { it.id !in disabled }

        val shortcutInfos = shortcutsToDisplay.map { item ->
            createShortcut(
                id = item.id,
                label = item.label,
                action = item.action,
                iconRes = item.iconRes,
                extras = item.extras
            )
        }

        ShortcutManagerCompat.setDynamicShortcuts(context, shortcutInfos)
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
