package com.dnfapps.arrmatey.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dnfapps.arrmatey.model.AppColor
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

class PreferencesStoreTest {
    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val dataStoreFactory = mockk<DataStoreFactory>()

    @Test
    fun testTabPreferencesMigration() =
        runTest {
            val file = tmpFolder.newFile("test.preferences_pb")
            val dataStore = PreferenceDataStoreFactory.create { file }

            every { dataStoreFactory.provideDataStore() } returns dataStore
            every { dataStoreFactory.defaultAppColor } returns AppColor.ArrMatey

            val preferencesStore = PreferencesStore(dataStoreFactory)

            // Set legacy tab preferences
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("tabPreferences")] = """{"bottomTabItems": ["LIBRARY", "SHOWS"]}"""
            }

            val tabs = preferencesStore.tabPreferences.first()
            assertEquals(2, tabs.orderedVisibleKeys.size)
            // Migration logic should add "standard_" prefix
            assertEquals("standard_LIBRARY", tabs.orderedVisibleKeys[0])
            assertEquals("standard_SHOWS", tabs.orderedVisibleKeys[1])
        }
}
