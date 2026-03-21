package com.dnfapps.arrmatey

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.dnfapps.arrmatey.di.appModules
import com.dnfapps.arrmatey.logging.LogFileManager
import com.dnfapps.arrmatey.logging.initLogging
import com.dnfapps.arrmatey.utils.CrashManager
import com.dnfapps.arrmatey.utils.initializeNetworkUtils
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ArrMateyApplication : Application(), SingletonImageLoader.Factory {

    private val imageLoader: ImageLoader by inject()
    private val crashManager: CrashManager by inject()

    override fun onCreate() {
        super.onCreate()

        initializeNetworkUtils(this)

        startKoin {
            androidContext(this@ArrMateyApplication)
            modules(appModules() + listOf(androidModule))
        }

        LogFileManager.initialize(this)
        crashManager.initialize()
    }

    override fun newImageLoader(
        context: PlatformContext
    ): ImageLoader = imageLoader
}