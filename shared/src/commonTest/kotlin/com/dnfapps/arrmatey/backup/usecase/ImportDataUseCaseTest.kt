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
}
