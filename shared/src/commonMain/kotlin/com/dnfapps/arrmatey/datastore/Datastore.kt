@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.dnfapps.arrmatey.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath {
        producePath().toPath()
    }

internal const val dataStoreFileName = "arrmatey.preferences_pb"
internal fun instanceDataStoreFileName(id: Long) = "arrmatey.instance_$id.preferences_pb"

expect class DataStoreFactory() {
    fun provideDataStore(): DataStore<Preferences>
    fun provideInstanceDataStore(instanceId: Long): DataStore<Preferences>
    fun providePlatformDataStore(): DataStore<Preferences>
}