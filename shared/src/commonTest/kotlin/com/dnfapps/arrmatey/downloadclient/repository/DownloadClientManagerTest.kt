package com.dnfapps.arrmatey.downloadclient.repository

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.downloadclient.api.DownloadClientApi
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.downloadclient.model.DownloadClientType
import io.ktor.client.plugins.logging.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DownloadClientManagerTest {
    private val fakeDao = FakeDownloadClientDao()
    private val repository = DownloadClientRepository(fakeDao)
    private val silentLogger =
        object : Logger {
            override fun log(message: String) = Unit
        }
    private val httpClientFactory = HttpClientFactory(Json { ignoreUnknownKeys = true }, silentLogger)

    private fun client(
        id: Long,
        password: String = "pw",
        apiKey: String = "",
    ) = DownloadClient(
        id = id,
        type = DownloadClientType.QBittorrent,
        label = "c$id",
        url = "http://localhost:$id",
        password = EncryptedString(password),
        apiKey = EncryptedString(apiKey),
    )

    // The manager collects on its own Dispatchers.Default scope, so tests await state changes.
    private suspend fun DownloadClientManager.awaitApis(
        predicate: (Map<Long, DownloadClientApi>) -> Boolean,
    ): Map<Long, DownloadClientApi> = withTimeout(5_000) { downloadClientApis.first(predicate) }

    @Test
    fun testAddingClientsCreatesApis(): Unit =
        runBlocking {
            val manager = DownloadClientManager(repository, httpClientFactory)

            fakeDao.emit(listOf(client(1), client(2)))
            val apis = manager.awaitApis { it.keys == setOf(1L, 2L) }

            assertNotNull(apis[1])
            assertNotNull(apis[2])
        }

    @Test
    fun testRemovingClientRemovesItsApi(): Unit =
        runBlocking {
            val manager = DownloadClientManager(repository, httpClientFactory)
            fakeDao.emit(listOf(client(1), client(2)))
            manager.awaitApis { it.keys == setOf(1L, 2L) }

            fakeDao.emit(listOf(client(1)))
            val apis = manager.awaitApis { it.keys == setOf(1L) }

            assertNull(apis[2])
        }

    @Test
    fun testCredentialChangeRebuildsApi(): Unit =
        runBlocking {
            val manager = DownloadClientManager(repository, httpClientFactory)
            fakeDao.emit(listOf(client(id = 1, password = "old")))
            val original = manager.awaitApis { 1L in it.keys }[1]
            assertNotNull(original)

            fakeDao.emit(listOf(client(id = 1, password = "new")))
            val rebuilt = manager.awaitApis { it[1] !== original }[1]

            assertNotNull(rebuilt)
            assertNotSame(original, rebuilt)
        }

    @Test
    fun testUnchangedClientKeepsSameApiInstance(): Unit =
        runBlocking {
            val manager = DownloadClientManager(repository, httpClientFactory)
            val unchanged = client(id = 1, password = "same")
            fakeDao.emit(listOf(unchanged))
            val first = manager.awaitApis { 1L in it.keys }[1]

            fakeDao.emit(listOf(unchanged))
            delay(50)
            val second = manager.downloadClientApis.value[1]

            assertNotNull(first)
            assertSame(first, second)
        }

    @Test
    fun testRefreshApiCreatesNewInstance(): Unit =
        runBlocking {
            val manager = DownloadClientManager(repository, httpClientFactory)
            fakeDao.emit(listOf(client(1)))
            manager.awaitApis { 1L in it.keys }
            val first = manager.getOrCreateApi(1)

            val refreshed = manager.refreshApi(1)

            assertNotNull(first)
            assertNotNull(refreshed)
            assertNotSame(first, refreshed)
        }

    @Test
    fun testRefreshApiReturnsNullForUnknownId(): Unit =
        runBlocking {
            val manager = DownloadClientManager(repository, httpClientFactory)

            val api = manager.refreshApi(999)

            assertNull(api)
        }

    @Test
    fun testGetOrCreateApiReturnsCachedInstance(): Unit =
        runBlocking {
            val manager = DownloadClientManager(repository, httpClientFactory)
            fakeDao.emit(listOf(client(1)))
            manager.awaitApis { 1L in it.keys }

            val a: DownloadClientApi? = manager.getOrCreateApi(1)
            val b: DownloadClientApi? = manager.getOrCreateApi(1)

            assertNotNull(a)
            assertTrue(a === b)
        }
}

private class FakeDownloadClientDao : DownloadClientDao {
    private val clients = MutableStateFlow<List<DownloadClient>>(emptyList())

    fun emit(list: List<DownloadClient>) {
        clients.value = list
    }

    override fun observeAllDownloadClients(): Flow<List<DownloadClient>> = clients

    override fun observeSelectedDownloadClient(): Flow<DownloadClient?> = MutableStateFlow(clients.value.firstOrNull { it.selected })

    override suspend fun getDownloadClientById(id: Long): DownloadClient? = clients.value.firstOrNull { it.id == id }

    override suspend fun getAllDownloadClients(): List<DownloadClient> = clients.value

    override suspend fun insert(downloadClient: DownloadClient): Long = throw NotImplementedError()

    override suspend fun delete(downloadClient: DownloadClient) = throw NotImplementedError()

    override suspend fun update(downloadClient: DownloadClient): Int = throw NotImplementedError()

    override suspend fun updateAll(downloadClients: List<DownloadClient>) = throw NotImplementedError()

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

    override suspend fun unselectAll() = Unit

    override suspend fun selectDownloadClient(id: Long) = Unit

    override suspend fun ensureFirstSelectedIfNone() = Unit
}
