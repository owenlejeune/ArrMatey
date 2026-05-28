package com.dnfapps.arrmatey.datastore

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class DataStoreFactory: KoinComponent {
    private val context: Context by inject()

    actual fun provideDataStore() = createDataStore(
        producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
    )

    actual fun provideInstanceDataStore(instanceId: Long) = createDataStore(
        producePath = {
            context.filesDir.resolve(instanceDataStoreFileName(instanceId)).absolutePath
        }
    )

    actual fun providePlatformDataStore() = createDataStore {
        context.filesDir.resolve("android_$dataStoreFileName").absolutePath
    }
}