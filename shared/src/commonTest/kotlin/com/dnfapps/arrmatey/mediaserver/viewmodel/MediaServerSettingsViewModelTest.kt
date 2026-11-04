package com.dnfapps.arrmatey.mediaserver.viewmodel

import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.usecase.PlexAuthUseCase
import com.dnfapps.arrmatey.mediaserver.database.MediaServerIntegrationDao
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.PlexServerConnection
import com.dnfapps.arrmatey.mediaserver.model.PlexServerResource
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.mediaserver.usecase.JellyfinQuickConnectUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logger
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeMediaServerDao : MediaServerIntegrationDao {
    private val data = MutableStateFlow<List<MediaServerIntegration>>(emptyList())

    override fun observeAll(): Flow<List<MediaServerIntegration>> = data.asStateFlow()

    override fun observeByType(type: MediaServerType): Flow<MediaServerIntegration?> = data.map { list -> list.firstOrNull { it.serverType == type } }

    override suspend fun getById(id: Long): MediaServerIntegration? = data.value.firstOrNull { it.id == id }

    override suspend fun getByType(type: MediaServerType): MediaServerIntegration? = data.value.firstOrNull { it.serverType == type }

    override suspend fun getAll(): List<MediaServerIntegration> = data.value

    override suspend fun insert(integration: MediaServerIntegration): Long {
        val list = data.value.filter { it.serverType != integration.serverType }.toMutableList()
        val newId = if (integration.id > 0) integration.id else (data.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val saved = integration.copy(id = newId)
        list.add(saved)
        data.value = list
        return newId
    }

    override suspend fun update(integration: MediaServerIntegration) {
        insert(integration)
    }

    override suspend fun delete(integration: MediaServerIntegration) {
        data.value = data.value.filter { it.id != integration.id }
    }

    override suspend fun deleteByType(type: MediaServerType) {
        data.value = data.value.filter { it.serverType != type }
    }
}

private class TestHttpClientFactory(
    private val json: Json,
    private val mockEngine: MockEngine,
    logger: Logger,
) : HttpClientFactory(json, logger) {
    override fun createGeneric(): HttpClient = HttpClient(mockEngine) {
        install(ContentNegotiation) {
            json(this@TestHttpClientFactory.json)
        }
    }

    override fun createMediaServerClient(integration: MediaServerIntegration): HttpClient = createGeneric()
}

@OptIn(ExperimentalCoroutinesApi::class)
class MediaServerSettingsViewModelTest {

    private var activeViewModel: MediaServerSettingsViewModel? = null
    private var activeManagerScope: CoroutineScope? = null

    private fun createViewModel(
        dao: FakeMediaServerDao = FakeMediaServerDao(),
        mockEngine: MockEngine = MockEngine { respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) },
    ): Triple<MediaServerSettingsViewModel, FakeMediaServerDao, MediaServerManager> {
        val testLogger = object : Logger {
            override fun log(message: String) = Unit
        }
        val json = Json { ignoreUnknownKeys = true }
        val httpFactory = TestHttpClientFactory(json, mockEngine, testLogger)
        val managerScope = CoroutineScope(Dispatchers.Default + Job())
        activeManagerScope = managerScope
        val manager = MediaServerManager(dao, httpFactory, managerScope)
        val plexAuthUseCase = PlexAuthUseCase(httpFactory)
        val jellyfinQuickConnectUseCase = JellyfinQuickConnectUseCase(httpFactory)

        val viewModel = MediaServerSettingsViewModel(
            mediaServerManager = manager,
            plexAuthUseCase = plexAuthUseCase,
            jellyfinQuickConnectUseCase = jellyfinQuickConnectUseCase,
        )
        activeViewModel = viewModel
        return Triple(viewModel, dao, manager)
    }

    @Test
    fun openConfigurationInitializesDefaultsForUnconfiguredPlex() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val (viewModel, _) = createViewModel()
            viewModel.openConfiguration(MediaServerType.Plex)
            advanceUntilIdle()

            val state = viewModel.configState.value
            assertNotNull(state)
            assertEquals(MediaServerType.Plex, state.serverType)
            assertEquals("http://localhost:32400", state.url)
            assertEquals("Plex", state.label)
            assertTrue(state.clientIdentifier.isNotEmpty())
            assertEquals(0L, state.existingId)
        } finally {
            activeViewModel?.viewModelScope?.cancel()
            activeManagerScope?.cancel()
        }
    }

    @Test
    fun openConfigurationLoadsExistingServerDetails() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dao = FakeMediaServerDao()
            dao.insert(
                MediaServerIntegration(
                    id = 10,
                    serverType = MediaServerType.Jellyfin,
                    label = "Home Jellyfin",
                    url = "http://192.168.1.100:8096",
                    token = EncryptedString("secret-jf-token"),
                    username = "jellyuser",
                    clientIdentifier = "jf-client-id",
                ),
            )
            val (viewModel, _) = createViewModel(dao = dao)
            viewModel.openConfiguration(MediaServerType.Jellyfin)
            advanceUntilIdle()

            val state = viewModel.configState.value
            assertNotNull(state)
            assertEquals(10L, state.existingId)
            assertEquals("Home Jellyfin", state.label)
            assertEquals("http://192.168.1.100:8096", state.url)
            assertEquals("secret-jf-token", state.token)
            assertEquals("jellyuser", state.username)
        } finally {
            activeViewModel?.viewModelScope?.cancel()
            activeManagerScope?.cancel()
        }
    }

    @Test
    fun saveConfigurationSavesToDatabase() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val (viewModel, dao) = createViewModel()
            viewModel.openConfiguration(MediaServerType.Emby)
            advanceUntilIdle()

            viewModel.updateUrl("http://emby.lan:8096")
            viewModel.updateToken("emby-api-key")
            viewModel.updateLabel("Emby Server")

            viewModel.saveConfiguration()
            advanceUntilIdle()

            val saved = dao.getByType(MediaServerType.Emby)
            assertNotNull(saved)
            assertEquals("Emby Server", saved.label)
            assertEquals("http://emby.lan:8096", saved.url)
            assertEquals("emby-api-key", saved.token.value)
            assertTrue(viewModel.configState.value?.saveCompleted == true)
        } finally {
            activeViewModel?.viewModelScope?.cancel()
            activeManagerScope?.cancel()
        }
    }

    @Test
    fun disconnectServerDeletesIntegration() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dao = FakeMediaServerDao()
            dao.insert(
                MediaServerIntegration(
                    id = 1,
                    serverType = MediaServerType.Plex,
                    label = "Plex",
                    url = "http://localhost:32400",
                    token = EncryptedString("token"),
                    clientIdentifier = "cid",
                ),
            )
            val (viewModel, _) = createViewModel(dao = dao)
            viewModel.openConfiguration(MediaServerType.Plex)
            advanceUntilIdle()
            assertNotNull(viewModel.configState.value)

            viewModel.disconnectServer(MediaServerType.Plex)
            advanceUntilIdle()

            assertNull(dao.getByType(MediaServerType.Plex))
            assertNull(viewModel.configState.value)
        } finally {
            activeViewModel?.viewModelScope?.cancel()
            activeManagerScope?.cancel()
        }
    }

    @Test
    fun selectDiscoveredPlexServerPopulatesUrlAndServerId() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val (viewModel, _) = createViewModel()
            viewModel.openConfiguration(MediaServerType.Plex)
            advanceUntilIdle()

            val resource = PlexServerResource(
                name = "My Living Room Plex",
                clientIdentifier = "plex-remote-id-1234",
                connections = listOf(
                    PlexServerConnection(
                        protocol = "https",
                        address = "192-168-1-50.plex.direct",
                        port = 32400,
                        uri = "https://192-168-1-50.plex.direct:32400",
                        local = true,
                    ),
                ),
            )

            viewModel.selectDiscoveredPlexServer(resource)

            val state = viewModel.configState.value
            assertNotNull(state)
            assertEquals("My Living Room Plex", state.label)
            assertEquals("plex-remote-id-1234", state.serverId)
            assertEquals("https://192-168-1-50.plex.direct:32400", state.url)
        } finally {
            activeViewModel?.viewModelScope?.cancel()
            activeManagerScope?.cancel()
        }
    }
}
