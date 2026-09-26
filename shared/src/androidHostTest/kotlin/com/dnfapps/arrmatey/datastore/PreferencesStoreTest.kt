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

    @Test
    fun testOverlayTabBackOpensDrawer() =
        runTest {
            val file = tmpFolder.newFile("test_overlay_back.preferences_pb")
            val dataStore = PreferenceDataStoreFactory.create { file }

            every { dataStoreFactory.provideDataStore() } returns dataStore
            every { dataStoreFactory.defaultAppColor } returns AppColor.ArrMatey

            val preferencesStore = PreferencesStore(dataStoreFactory)

            // Default is true
            assertEquals(true, preferencesStore.overlayTabBackOpensDrawer.first())

            // Set to false via dataStore
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.OVERLAY_TAB_BACK_OPENS_DRAWER] = false
            }
            assertEquals(false, preferencesStore.overlayTabBackOpensDrawer.first())

            // Set to true via dataStore
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.OVERLAY_TAB_BACK_OPENS_DRAWER] = true
            }
            assertEquals(true, preferencesStore.overlayTabBackOpensDrawer.first())
        }

    @Test
    fun testQueueRemovalPreferences() =
        runTest {
            val file = tmpFolder.newFile("test_queue_removal.preferences_pb")
            val dataStore = PreferenceDataStoreFactory.create { file }

            every { dataStoreFactory.provideDataStore() } returns dataStore
            every { dataStoreFactory.defaultAppColor } returns AppColor.ArrMatey

            val preferencesStore = PreferencesStore(dataStoreFactory)

            // Skipping redownload defaults to true, unlike the other two
            assertEquals(QueueRemovalPreferences(), preferencesStore.queueRemovalPreferences.first())

            // Set one key at a time so a mix-up between them can't pass
            dataStore.edit { prefs ->
                prefs[PreferenceKeys.QUEUE_REMOVE_FROM_CLIENT] = true
            }
            assertEquals(
                QueueRemovalPreferences(removeFromClient = true),
                preferencesStore.queueRemovalPreferences.first(),
            )

            dataStore.edit { prefs ->
                prefs[PreferenceKeys.QUEUE_ADD_TO_BLOCKLIST] = true
            }
            assertEquals(
                QueueRemovalPreferences(removeFromClient = true, addToBlocklist = true),
                preferencesStore.queueRemovalPreferences.first(),
            )

            dataStore.edit { prefs ->
                prefs[PreferenceKeys.QUEUE_SKIP_REDOWNLOAD] = false
            }
            assertEquals(
                QueueRemovalPreferences(removeFromClient = true, addToBlocklist = true, skipRedownload = false),
                preferencesStore.queueRemovalPreferences.first(),
            )
        }

    @Test
    fun testDownloadDeleteFiles() =
        runTest {
            val file = tmpFolder.newFile("test_download_delete_files.preferences_pb")
            val dataStore = PreferenceDataStoreFactory.create { file }

            every { dataStoreFactory.provideDataStore() } returns dataStore
            every { dataStoreFactory.defaultAppColor } returns AppColor.ArrMatey

            val preferencesStore = PreferencesStore(dataStoreFactory)

            assertEquals(false, preferencesStore.downloadDeleteFiles.first())

            dataStore.edit { prefs ->
                prefs[PreferenceKeys.DOWNLOAD_DELETE_FILES] = true
            }
            assertEquals(true, preferencesStore.downloadDeleteFiles.first())
        }
}
