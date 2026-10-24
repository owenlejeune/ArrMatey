package com.dnfapps.arrmatey.backup.usecase

import com.dnfapps.arrmatey.backup.TransportEncryptor
import com.dnfapps.arrmatey.backup.model.BackupExport
import com.dnfapps.arrmatey.backup.model.CustomWebpageExport
import com.dnfapps.arrmatey.backup.viewmodel.BackupViewModel
import com.dnfapps.arrmatey.database.dao.CustomWebpageDao
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.datastore.DataStoreFactory
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.datastore.TabPreferences
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.webpage.model.CustomWebpage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ImportDataUseCaseTest {
    private val fakeEncryptor =
        object : TransportEncryptor {
            override fun encrypt(
                data: String,
                password: String,
            ): String = data

            override fun decrypt(
                encryptedData: String,
                password: String,
            ): String = encryptedData
        }

    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    private val dummyInstanceDao =
        object : InstanceDao {
            override suspend fun insert(instance: Instance): Long = 0

            override suspend fun delete(instance: Instance) {}

            override suspend fun update(instance: Instance): Int = 0

            override suspend fun updateAll(instances: List<Instance>) {}

            override fun observeAllInstances(): Flow<List<Instance>> = emptyFlow()

            override fun observeInstancesByType(type: InstanceType): Flow<List<Instance>> = emptyFlow()

            override fun observeInstancesByTypes(types: List<InstanceType>): Flow<List<Instance>> = emptyFlow()

            override suspend fun getAllInstances(): List<Instance> = emptyList()

            override suspend fun getInstanceById(id: Long): Instance? = null

            override fun observeSelectedInstance(type: InstanceType): Flow<Instance?> = emptyFlow()

            override fun observeSelectedInstanceByTypes(types: List<InstanceType>): Flow<Instance?> = emptyFlow()

            override suspend fun getInstancesOfType(type: InstanceType): List<Instance> = emptyList()

            override suspend fun getInstancesOfTypes(types: List<InstanceType>): List<Instance> = emptyList()

            override suspend fun unselectAllOf(type: InstanceType) {}

            override suspend fun unselectAllOfTypes(types: List<InstanceType>) {}

            override suspend fun ensureFirstSelectedIfNone(type: InstanceType) {}

            override suspend fun ensureFirstSelectedIfNone(types: List<InstanceType>) {}

            override suspend fun selectInstance(id: Long) {}

            override suspend fun findByUrl(url: String): Long? = null

            override suspend fun findByLabel(label: String): Long? = null

            override suspend fun findOtherByUrl(
                url: String,
                currentId: Long,
            ): Long? = null

            override suspend fun findOtherByLabel(
                label: String,
                currentId: Long,
            ): Long? = null
        }

    private val dummyDownloadClientDao =
        object : DownloadClientDao {
            override suspend fun insert(downloadClient: DownloadClient): Long = 0

            override suspend fun delete(downloadClient: DownloadClient) {}

            override suspend fun update(downloadClient: DownloadClient): Int = 0

            override suspend fun updateAll(downloadClients: List<DownloadClient>) {}

            override fun observeAllDownloadClients(): Flow<List<DownloadClient>> = emptyFlow()

            override fun observeSelectedDownloadClient(): Flow<DownloadClient?> = emptyFlow()

            override suspend fun getDownloadClientById(id: Long): DownloadClient? = null

            override suspend fun getAllDownloadClients(): List<DownloadClient> = emptyList()

            override suspend fun findByUrl(url: String): Long? = null

            override suspend fun findByLabel(label: String): Long? = null

            override suspend fun findOtherByUrl(
                url: String,
                currentId: Long,
            ): Long? = null

            override suspend fun findOtherByLabel(
                label: String,
                currentId: Long,
            ): Long? = null

            override suspend fun unselectAll() {}

            override suspend fun selectDownloadClient(id: Long) {}

            override suspend fun ensureFirstSelectedIfNone() {}
        }

    private val dummyCustomWebpageDao =
        object : CustomWebpageDao {
            override fun getAllWebpages(): Flow<List<CustomWebpage>> = emptyFlow()

            override suspend fun getAllWebpagesList(): List<CustomWebpage> = emptyList()

            override suspend fun getWebpageById(id: Long): CustomWebpage? = null

            override fun observeWebpageById(id: Long): Flow<CustomWebpage?> = emptyFlow()

            override suspend fun insert(webpage: CustomWebpage): Long = 0

            override suspend fun update(webpage: CustomWebpage): Int = 0

            override suspend fun delete(webpage: CustomWebpage) {}

            override suspend fun deleteById(id: Long) {}
        }

    private val dataStoreFactory = DataStoreFactory()

    @Test
    fun testDecryptBackupWithLegacyBooksehlfTypo() {
        val legacyJsonBackup =
            """
            {
                "instances": [
                    {
                        "type": "Booksehlf",
                        "label": "My Books",
                        "url": "http://192.168.1.100:8787",
                        "apiKey": "test-key",
                        "noApiKeyRequired": false,
                        "enabled": true,
                        "slowInstance": false,
                        "notificationsEnabled": false,
                        "headers": [],
                        "localNetworkEnabled": false,
                        "localNetworkSsids": []
                    }
                ],
                "downloadClients": []
            }
            """.trimIndent()

        val importDataUseCase =
            ImportDataUseCase(
                instanceDao = dummyInstanceDao,
                downloadClientDao = dummyDownloadClientDao,
                customWebpageDao = dummyCustomWebpageDao,
                instancePreferenceStoreRepository = InstancePreferenceStoreRepository(dataStoreFactory),
                preferencesStore = PreferencesStore(dataStoreFactory),
                transportEncryptor = fakeEncryptor,
                json = json,
            )

        val result = importDataUseCase.decryptBackup(legacyJsonBackup, "password")

        assertNotNull(result)
        assertEquals(1, result.instances.size)
        assertEquals(InstanceType.Bookshelf, result.instances.first().type)
        assertEquals("My Books", result.instances.first().label)
    }

    @Test
    fun testDecryptBackupWithLegacyBooksehelfTypo() {
        val legacyJsonBackup =
            """
            {
                "instances": [
                    {
                        "type": "Booksehelf",
                        "label": "My Books 2",
                        "url": "http://192.168.1.101:8787",
                        "apiKey": "test-key-2",
                        "noApiKeyRequired": false,
                        "enabled": true,
                        "slowInstance": false,
                        "notificationsEnabled": false,
                        "headers": [],
                        "localNetworkEnabled": false,
                        "localNetworkSsids": []
                    }
                ],
                "downloadClients": []
            }
            """.trimIndent()

        val importDataUseCase =
            ImportDataUseCase(
                instanceDao = dummyInstanceDao,
                downloadClientDao = dummyDownloadClientDao,
                customWebpageDao = dummyCustomWebpageDao,
                instancePreferenceStoreRepository = InstancePreferenceStoreRepository(dataStoreFactory),
                preferencesStore = PreferencesStore(dataStoreFactory),
                transportEncryptor = fakeEncryptor,
                json = json,
            )

        val result = importDataUseCase.decryptBackup(legacyJsonBackup, "password")

        assertNotNull(result)
        assertEquals(1, result.instances.size)
        assertEquals(InstanceType.Bookshelf, result.instances.first().type)
        assertEquals("My Books 2", result.instances.first().label)
    }

    @Test
    fun testImportSelectedSetsSelectedOnFirstInstanceAndDownloadClient() = runTest {
        val insertedInstances = mutableListOf<Instance>()
        val insertedClients = mutableListOf<DownloadClient>()
        var ensureFirstSelectedDownloadClientCalled = false

        val testInstanceDao = object : InstanceDao by dummyInstanceDao {
            override suspend fun getInstancesOfType(type: InstanceType): List<Instance> = insertedInstances.filter { it.type == type }

            override suspend fun insert(instance: Instance): Long {
                insertedInstances.add(instance)
                return insertedInstances.size.toLong()
            }
        }

        val testDownloadClientDao = object : DownloadClientDao by dummyDownloadClientDao {
            override suspend fun getAllDownloadClients(): List<DownloadClient> = insertedClients

            override suspend fun insert(downloadClient: DownloadClient): Long {
                insertedClients.add(downloadClient)
                return insertedClients.size.toLong()
            }

            override suspend fun ensureFirstSelectedIfNone() {
                ensureFirstSelectedDownloadClientCalled = true
            }
        }

        val importUseCase = ImportDataUseCase(
            instanceDao = testInstanceDao,
            downloadClientDao = testDownloadClientDao,
            customWebpageDao = dummyCustomWebpageDao,
            instancePreferenceStoreRepository = InstancePreferenceStoreRepository(dataStoreFactory),
            preferencesStore = PreferencesStore(dataStoreFactory),
            transportEncryptor = fakeEncryptor,
            json = json,
        )

        val jsonBackup = """
            {
                "instances": [
                    {
                        "type": "Sonarr",
                        "label": "Sonarr 1",
                        "url": "http://192.168.1.100:8989",
                        "apiKey": "key1"
                    }
                ],
                "downloadClients": [
                    {
                        "type": "QBittorrent",
                        "label": "qBit 1",
                        "url": "http://192.168.1.100:8080"
                    }
                ]
            }
        """.trimIndent()

        val backup = importUseCase.decryptBackup(jsonBackup, "password")
        importUseCase.importSelected(
            backup = backup,
            selectedInstanceIndices = setOf(0),
            selectedDownloadClientIndices = setOf(0),
            importTabPreferences = false,
            importUiPreferences = false,
            importIntegrationsPreferences = false,
        )

        assertEquals(1, insertedInstances.size)
        assertEquals(true, insertedInstances.first().selected)

        assertEquals(1, insertedClients.size)
        assertEquals(true, insertedClients.first().selected)
        assertEquals(true, ensureFirstSelectedDownloadClientCalled)
    }

    @Test
    fun customWebpageRestoreReusesExistingPages() = runTest {
        val webpages = mutableListOf<CustomWebpage>()
        var nextId = 20L
        val testWebpageDao = object : CustomWebpageDao by dummyCustomWebpageDao {
            override suspend fun getAllWebpagesList(): List<CustomWebpage> = webpages.toList()

            override suspend fun insert(webpage: CustomWebpage): Long {
                val id = nextId++
                webpages += webpage.copy(id = id)
                return id
            }

            override suspend fun update(webpage: CustomWebpage): Int {
                val index = webpages.indexOfFirst { it.id == webpage.id }
                if (index < 0) return 0
                webpages[index] = webpage
                return 1
            }
        }
        val importUseCase = ImportDataUseCase(
            instanceDao = dummyInstanceDao,
            downloadClientDao = dummyDownloadClientDao,
            customWebpageDao = testWebpageDao,
            instancePreferenceStoreRepository = InstancePreferenceStoreRepository(dataStoreFactory),
            preferencesStore = PreferencesStore(dataStoreFactory),
            transportEncryptor = fakeEncryptor,
            json = json,
        )
        val backup = BackupExport(
            customWebpages = listOf(
                CustomWebpageExport(id = 1, name = "Audit", url = "https://example.com", allowSelfSignedCerts = true),
                CustomWebpageExport(id = 2, name = "Second", url = "https://example.net"),
            ),
        )

        repeat(2) {
            importUseCase.importSelected(
                backup = backup,
                selectedInstanceIndices = emptySet(),
                selectedDownloadClientIndices = emptySet(),
                selectedCustomWebpageIndices = setOf(0, 1),
                importTabPreferences = false,
                importUiPreferences = false,
                importIntegrationsPreferences = false,
            )
        }

        assertEquals(2, webpages.size)
        assertEquals(true, webpages.first().allowSelfSignedCerts)
        assertEquals(20L, webpages.first().id)

        val remapped = remapCustomWebpageTabPreferences(
            preferences = TabPreferences(
                orderedVisibleKeys = listOf("standard_DASHBOARD", "webpage_1", "webpage_2", "webpage_99"),
                orderedHiddenKeys = listOf("webpage_2"),
                orderedRemovedKeys = listOf("webpage_1"),
            ),
            exportedWebpages = backup.customWebpages,
            importedIds = mapOf(1L to 20L),
        )
        assertEquals(listOf("standard_DASHBOARD", "webpage_20"), remapped.orderedVisibleKeys)
        assertEquals(emptyList(), remapped.orderedHiddenKeys)
        assertEquals(listOf("webpage_20"), remapped.orderedRemovedKeys)
    }

    @Test
    fun customWebpageExportDecodesLegacyAndNewFields() {
        val legacy = json.decodeFromString<CustomWebpageExport>("""{"name":"Legacy","url":"https://example.com"}""")
        assertEquals(null, legacy.id)
        assertEquals(false, legacy.allowSelfSignedCerts)

        val current = json.decodeFromString<CustomWebpageExport>(
            """{"id":42,"name":"Current","url":"https://example.org","allowSelfSignedCerts":true}""",
        )
        assertEquals(42L, current.id)
        assertEquals(true, current.allowSelfSignedCerts)
    }

    @Test
    fun exportIncludesCustomWebpageIdAndSelfSignedSetting() = runTest {
        val webpageDao = object : CustomWebpageDao by dummyCustomWebpageDao {
            override suspend fun getAllWebpagesList(): List<CustomWebpage> = listOf(
                CustomWebpage(id = 42, name = "TLS page", url = "https://example.com", allowSelfSignedCerts = true),
            )
        }
        val exportUseCase = ExportDataUseCase(
            instanceDao = dummyInstanceDao,
            downloadClientDao = dummyDownloadClientDao,
            customWebpageDao = webpageDao,
            instancePreferenceStoreRepository = InstancePreferenceStoreRepository(dataStoreFactory),
            preferencesStore = PreferencesStore(dataStoreFactory),
            transportEncryptor = fakeEncryptor,
            json = json,
        )
        val encrypted = exportUseCase(
            password = "password",
            selectedInstanceIds = emptySet(),
            selectedDownloadClientIds = emptySet(),
            selectedCustomWebpageIds = setOf(42),
            includeInstancePreferences = false,
            includeTabPreferences = false,
            includeUiPreferences = false,
            includeIntegrationsPreferences = false,
        )
        val exported = json.decodeFromString<BackupExport>(encrypted).customWebpages.single()

        assertEquals(42L, exported.id)
        assertEquals(true, exported.allowSelfSignedCerts)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun importSessionClearsPreviousBackupAndRefreshesExportedPages() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val pages = mutableListOf<CustomWebpage>()
            val pageDao = object : CustomWebpageDao by dummyCustomWebpageDao {
                override suspend fun getAllWebpagesList(): List<CustomWebpage> = pages.toList()
                override suspend fun insert(webpage: CustomWebpage): Long {
                    val id = pages.size.toLong() + 1
                    pages += webpage.copy(id = id)
                    return id
                }
            }
            val instancePreferences = InstancePreferenceStoreRepository(dataStoreFactory)
            val preferences = PreferencesStore(dataStoreFactory)
            val importer = ImportDataUseCase(dummyInstanceDao, dummyDownloadClientDao, pageDao, instancePreferences, preferences, fakeEncryptor, json)
            val exporter = ExportDataUseCase(dummyInstanceDao, dummyDownloadClientDao, pageDao, instancePreferences, preferences, fakeEncryptor, json)
            val viewModel = BackupViewModel(exporter, importer, dummyInstanceDao, dummyDownloadClientDao, pageDao)
            advanceUntilIdle()
            val backup = """{"customWebpages":[{"name":"Audit","url":"https://example.com"}]}"""
            viewModel.setImportPassword("test")
            viewModel.prepareImport(backup)
            advanceUntilIdle()
            assertEquals(1, viewModel.importUiState.value.decryptedBackup?.customWebpages?.size)
            viewModel.resetImportState()
            assertEquals(null, viewModel.importUiState.value.decryptedBackup)
            assertEquals("", viewModel.importUiState.value.password)
            viewModel.setImportPassword("test")
            viewModel.prepareImport(backup)
            advanceUntilIdle()
            viewModel.prepareImport("invalid JSON")
            advanceUntilIdle()
            assertEquals(null, viewModel.importUiState.value.decryptedBackup)
            assertEquals(emptySet(), viewModel.importUiState.value.selectedCustomWebpageIndices)
            assertNotNull(viewModel.importUiState.value.error)
            viewModel.prepareImport(backup)
            advanceUntilIdle()
            var complete = false
            viewModel.executeImport { complete = true }
            advanceUntilIdle()
            assertEquals(true, complete)
            assertEquals(null, viewModel.importUiState.value.decryptedBackup)
            assertEquals(1, viewModel.exportUiState.value.customWebpages.size)
            assertEquals(setOf(1L), viewModel.exportUiState.value.selectedCustomWebpageIds)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
