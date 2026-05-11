package com.dnfapps.arrmatey

import coil3.ImageLoader
import com.dnfapps.arrmatey.navigation.AudiobooksTabNavigation
import com.dnfapps.arrmatey.navigation.BooksTabNavigation
import com.dnfapps.arrmatey.navigation.MoviesTabNavigation
import com.dnfapps.arrmatey.navigation.MusicTabNavigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.RequestsTabNavigation
import com.dnfapps.arrmatey.navigation.SeriesTabNavigation
import com.dnfapps.arrmatey.navigation.SettingsNavigation
import com.dnfapps.arrmatey.ui.helpers.ArrImageLoader
import com.dnfapps.arrmatey.utils.AndroidCrashManager
import com.dnfapps.arrmatey.utils.CrashManager
import org.koin.dsl.module

val androidModule = module {
    single { SettingsNavigation() }

    single { SeriesTabNavigation() }
    single { MoviesTabNavigation() }
    single { MusicTabNavigation() }
    single { RequestsTabNavigation() }
    single { BooksTabNavigation() }
    single { AudiobooksTabNavigation() }

    single { NavigationManager(get(), get(), get(), get(), get(), get(), get()) }

    single<ImageLoader> {
        ArrImageLoader(get(), get())
            .imageLoader
    }

    single<CrashManager> {
        AndroidCrashManager()
    }
}