package com.dnfapps.arrmatey.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dnfapps.arrmatey.model.AppColor
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PreferencesStoreTest {
    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val dataStoreFactory = mockk<DataStoreFactory>()

    // Writes are launched and not awaited, so join them rather than polling for the result.
    private suspend fun CoroutineScope.awaitWrites() =
        coroutineContext.job.children
            .toList()
            .forEach { it.join() }

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

    @Test
    fun testWriteIsVisibleOnceComplete() =
        runBlocking {
            val file = tmpFolder.newFile("test_write.preferences_pb")
            val dataStore = PreferenceDataStoreFactory.create { file }

            every { dataStoreFactory.provideDataStore() } returns dataStore
            every { dataStoreFactory.defaultAppColor } returns AppColor.ArrMatey

            val scope = defaultPreferencesScope()
            val preferencesStore = PreferencesStore(dataStoreFactory, scope)

            assertEquals(false, preferencesStore.useColoredActivityCards.first())

            preferencesStore.setUseColoredActivityCards(true)
            scope.awaitWrites()

            assertEquals(true, preferencesStore.useColoredActivityCards.first())
        }

    @Test
    fun testFailedWriteDoesNotStopLaterWrites() =
        runBlocking {
            val failingDataStore = mockk<DataStore<Preferences>>()
            every { failingDataStore.data } returns flowOf(emptyPreferences())
            coEvery { failingDataStore.updateData(any()) } throws IOException("disk full")

            every { dataStoreFactory.provideDataStore() } returns failingDataStore
            every { dataStoreFactory.defaultAppColor } returns AppColor.ArrMatey

            // Keeps the production SupervisorJob, but contains the deliberate failure below
            val scope = CoroutineScope(defaultPreferencesScope().coroutineContext + CoroutineExceptionHandler { _, _ -> })
            val preferencesStore = PreferencesStore(dataStoreFactory, scope)

            preferencesStore.setUseColoredActivityCards(true)
            scope.awaitWrites()

            // A plain Job here would leave the scope cancelled, silently dropping every later write
            assertTrue(scope.isActive)

            preferencesStore.setHideInstanceSwitcher(true)
            scope.awaitWrites()
            assertTrue(scope.isActive)
        }
}
