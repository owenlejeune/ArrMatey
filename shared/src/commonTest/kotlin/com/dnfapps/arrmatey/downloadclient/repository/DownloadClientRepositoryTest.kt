package com.dnfapps.arrmatey.downloadclient.repository

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientInsertResult
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DownloadClientRepositoryTest {
    private val fakeDao =
        object : DownloadClientDao {
            private val clients = MutableStateFlow<List<DownloadClient>>(emptyList())

            override suspend fun insert(downloadClient: DownloadClient): Long {
                val id = (clients.value.size + 1).toLong()
                clients.value += downloadClient.copy(id = id)
                return id
            }

            override suspend fun delete(downloadClient: DownloadClient) {
                clients.value = clients.value.filter { it.id != downloadClient.id }
            }

            override suspend fun update(downloadClient: DownloadClient): Int {
                clients.value = clients.value.map { if (it.id == downloadClient.id) downloadClient else it }
                return 1
            }

            override suspend fun updateAll(downloadClients: List<DownloadClient>) {
                this.clients.value = downloadClients
            }

            override fun observeAllDownloadClients(): Flow<List<DownloadClient>> = clients

            override fun observeSelectedDownloadClient(): Flow<DownloadClient?> = MutableStateFlow(clients.value.find { it.selected })

            override suspend fun getDownloadClientById(id: Long): DownloadClient? = clients.value.find { it.id == id }

            override suspend fun getAllDownloadClients(): List<DownloadClient> = clients.value

            override suspend fun findByUrl(url: String): Long? = clients.value.find { it.url == url }?.id

            override suspend fun findByLabel(label: String): Long? = clients.value.find { it.label == label }?.id

            override suspend fun findOtherByUrl(
                url: String,
                currentId: Long,
            ): Long? = clients.value.find { it.url == url && it.id != currentId }?.id

            override suspend fun findOtherByLabel(
                label: String,
                currentId: Long,
            ): Long? = clients.value.find { it.label == label && it.id != currentId }?.id

            override suspend fun unselectAll() {
                clients.value = clients.value.map { it.copy(selected = false) }
            }

            override suspend fun selectDownloadClient(id: Long) {
                clients.value = clients.value.map { it.copy(selected = it.id == id) }
            }

            override suspend fun ensureFirstSelectedIfNone() {
                if (clients.value.none { it.selected }) {
                    val first = clients.value.firstOrNull()
                    if (first != null) {
                        selectDownloadClient(first.id)
                    }
                }
            }
        }

    private val repository = DownloadClientRepository(fakeDao)

    @Test
    fun testCreateDownloadClientSuccess() =
        runTest {
            val client =
                DownloadClient(
                    label = "Test Client",
                    url = "http://localhost:8080",
                    type = DownloadClientType.QBittorrent,
                    apiKey = EncryptedString(""),
                )
            val result = repository.createDownloadClient(client)
            assertTrue(result is DownloadClientInsertResult.Success)
            assertEquals(1, result.id)
        }

    @Test
    fun testCreateDownloadClientConflict() =
        runTest {
            val client =
                DownloadClient(
                    label = "Test Client",
                    url = "http://localhost:8080",
                    type = DownloadClientType.QBittorrent,
                    apiKey = EncryptedString(""),
                )
            repository.createDownloadClient(client)

            val result = repository.createDownloadClient(client)
            assertTrue(result is DownloadClientInsertResult.Conflict)
        }
}
