package com.dnfapps.arrmatey.backup.usecase

import com.dnfapps.arrmatey.backup.TransportEncryptor
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.datastore.DataStoreFactory
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
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

            override suspend fun getAllInstances(): List<Instance> = emptyList()

            override suspend fun getInstanceById(id: Long): Instance? = null

            override fun observeSelectedInstance(type: InstanceType): Flow<Instance?> = emptyFlow()

            override suspend fun getInstancesOfType(type: InstanceType): List<Instance> = emptyList()

            override suspend fun unselectAllOf(type: InstanceType) {}

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

            override suspend fun ensureFirstSelectedIfNone(type: InstanceType) {}
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
        object : com.dnfapps.arrmatey.database.dao.CustomWebpageDao {
            override fun getAllWebpages(): Flow<List<com.dnfapps.arrmatey.webpage.model.CustomWebpage>> = emptyFlow()

            override suspend fun getAllWebpagesList(): List<com.dnfapps.arrmatey.webpage.model.CustomWebpage> = emptyList()

            override suspend fun getWebpageById(id: Long): com.dnfapps.arrmatey.webpage.model.CustomWebpage? = null

            override fun observeWebpageById(id: Long): Flow<com.dnfapps.arrmatey.webpage.model.CustomWebpage?> = emptyFlow()

            override suspend fun insert(webpage: com.dnfapps.arrmatey.webpage.model.CustomWebpage): Long = 0

            override suspend fun update(webpage: com.dnfapps.arrmatey.webpage.model.CustomWebpage): Int = 0

            override suspend fun delete(webpage: com.dnfapps.arrmatey.webpage.model.CustomWebpage) {}

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
}
