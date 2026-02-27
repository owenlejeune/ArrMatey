package com.dnfapps.arrmatey.navigation

import androidx.compose.runtime.mutableStateListOf
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class Navigation<T>(initialScreen: T) {
    val backStack = mutableStateListOf(initialScreen)

    fun navigateTo(screen: T) {
        backStack.add(screen)
    }

    fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    fun replaceCurrent(screen: T) {
        backStack.add(screen)
        backStack.removeAt(backStack.size - 2)
    }

    fun replaceBackStack(newStack: List<T>) {
        backStack.removeRange(fromIndex = 1, toIndex = backStack.size)
        backStack.addAll(newStack)
    }
}

class SettingsNavigation: Navigation<SettingsScreen>(SettingsScreen.Landing) {
    fun onInstanceTap(id: Long, type: InstanceType) {
        when (type) {
            InstanceType.Sonarr,
            InstanceType.Radarr,
            InstanceType.Lidarr -> {
                navigateTo(SettingsScreen.ArrDashboard(id))
            }
            else -> navigateTo(SettingsScreen.EditInstance(id))
        }
    }
}
class SeriesTabNavigation : Navigation<ArrScreen>(ArrScreen.Library)
class MoviesTabNavigation: Navigation<ArrScreen>(ArrScreen.Library)
class MusicTabNavigation: Navigation<ArrScreen>(ArrScreen.Library)
class RequestsTabNavigation: Navigation<SeerrScreen>(SeerrScreen.Home)