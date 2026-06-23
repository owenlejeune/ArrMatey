package com.dnfapps.arrmatey

import coil3.ImageLoader
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.datastore.AndroidPreferencesStore
import com.dnfapps.arrmatey.navigation.AppState
import com.dnfapps.arrmatey.navigation.AudiobooksTabNavigator
import com.dnfapps.arrmatey.navigation.BazarrTabNavigator
import com.dnfapps.arrmatey.navigation.BooksTabNavigator
import com.dnfapps.arrmatey.navigation.DashboardTabNavigator
import com.dnfapps.arrmatey.navigation.MoviesTabNavigator
import com.dnfapps.arrmatey.navigation.MusicTabNavigator
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.navigation.RequestsTabNavigator
import com.dnfapps.arrmatey.navigation.SeriesTabNavigator
import com.dnfapps.arrmatey.navigation.SettingsTabNavigator
import com.dnfapps.arrmatey.ui.helpers.ArrImageLoader
import com.dnfapps.arrmatey.utils.AndroidCrashManager
import com.dnfapps.arrmatey.utils.CrashManager
import com.dnfapps.arrmatey.shortcuts.AppShortcutManager
import org.koin.dsl.module

val androidModule = module {
    single { AppState() }

    single { AndroidPreferencesStore(get()) }

    // Shortcuts
    single { AppShortcutManager(get(), get(), get(), get(), get()) }

    // Navigators
    single { SettingsTabNavigator() }
    single { SeriesTabNavigator() }
    single { MoviesTabNavigator() }
    single { MusicTabNavigator() }
    single { RequestsTabNavigator() }
    single { BooksTabNavigator() }
    single { AudiobooksTabNavigator() }
    single { DashboardTabNavigator() }
    single { BazarrTabNavigator() }

    // Navigation Manager
    single {
        val registry: Map<TabItem, Navigator<*>> = mapOf(
            TabItem.Standard.SHOWS to get<SeriesTabNavigator>(),
            TabItem.Standard.MOVIES to get<MoviesTabNavigator>(),
            TabItem.Standard.MUSIC to get<MusicTabNavigator>(),
            TabItem.Standard.REQUESTS to get<RequestsTabNavigator>(),
            TabItem.Standard.BOOKS to get<BooksTabNavigator>(),
            TabItem.Standard.AUDIOBOOKS to get<AudiobooksTabNavigator>(),
        )
        NavigationManager(registry, get(), get(), get(), get(), get(), get())
    }

    // Others
    single<ImageLoader> {
        ArrImageLoader(get(), get())
            .imageLoader
    }

    single<CrashManager> {
        AndroidCrashManager()
    }
}
