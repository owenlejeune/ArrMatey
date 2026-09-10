package com.dnfapps.arrmatey

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.dnfapps.arrmatey.di.appModules
import com.dnfapps.arrmatey.logging.LogFileManager
import com.dnfapps.arrmatey.utils.CrashManager
import com.dnfapps.arrmatey.utils.initializeNetworkUtils
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.context.startKoin

class ArrMateyApplication :
    Application(),
    KoinComponent,
    SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()

        LogFileManager.initialize(this)
        initializeNetworkUtils(this)

        startKoin {
            androidContext(this@ArrMateyApplication)
            modules(appModules() + listOf(androidModule))
        }

        val crashManager: CrashManager by inject()
        crashManager.initialize()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader = get()
}
