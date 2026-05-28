package com.dnfapps.arrmatey

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.compose.SeerrTab
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.toSearch
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.shortcuts.AppShortcutManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val shortcutManager: AppShortcutManager by inject()
    private val navigationManager: NavigationManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            shortcutManager.updateShortcuts()
        }

        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.action?.let { action ->
            when (action) {
                AppShortcutManager.ACTION_OPEN_DOWNLOADS -> {
                    navigationManager.navigateToTab(TabItem.Standard.DOWNLOADS)
                }
                AppShortcutManager.ACTION_OPEN_ACTIVITY -> {
                    navigationManager.navigateToTab(TabItem.Standard.ACTIVITY)
                }
                AppShortcutManager.ACTION_OPEN_SCHEDULE -> {
                    navigationManager.navigateToTab(TabItem.Standard.CALENDAR)
                }
                AppShortcutManager.ACTION_OPEN_REQUESTS -> {
                    navigationManager.navigateToTab(TabItem.Standard.REQUESTS)
                }
                AppShortcutManager.ACTION_OPEN_LIBRARY -> {
                    val typeName = intent.getStringExtra(AppShortcutManager.EXTRA_INSTANCE_TYPE)
                    val type = InstanceType.entries.find { it.name == typeName }
                    type?.let { 
                        navigationManager.arr(it).popToRoot()
                        navigationManager.navigateToTab(navigationManager.tabFor(it))
                    }
                }
                AppShortcutManager.ACTION_OPEN_SEARCH -> {
                    val typeName = intent.getStringExtra(AppShortcutManager.EXTRA_INSTANCE_TYPE)
                    val type = InstanceType.entries.find { it.name == typeName }
                    type?.let { 
                        val navigator = navigationManager.arr(it)
                        navigator.popToRoot()
                        navigator.toSearch()
                        navigationManager.navigateToTab(navigationManager.tabFor(it))
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        get<InstanceManager>().cleanup()
        get<ActivityQueueService>().cleanup()
    }
}