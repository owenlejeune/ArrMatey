package com.dnfapps.arrmatey.mediaserver.viewmodel

import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.compose.utils.SortBy
import com.dnfapps.arrmatey.compose.utils.SortOrder
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.mediaserver.database.MediaServerIntegrationDao
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import com.dnfapps.arrmatey.mediaserver.model.WatchedFilter
import com.dnfapps.arrmatey.mediaserver.repository.MediaServerManager
import com.dnfapps.arrmatey.ui.theme.ViewType
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

private class FakeLibraryMediaServerDao(
    initialData: List<MediaServerIntegration> = emptyList(),
) : MediaServerIntegrationDao {
    private val data = MutableStateFlow<List<MediaServerIntegration>>(initialData)

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

private class TestLibraryHttpClientFactory(
    private val json: Json,
    private val mockEngine: MockEngine,
    logger: Logger,
) : HttpClientFactory(json, logger) {
    override fun createGeneric(): HttpClient = HttpClient(mockEngine) {
        install(ContentNegotiation) {
            json(this@TestLibraryHttpClientFactory.json)
        }
    }

    override fun createMediaServerClient(integration: MediaServerIntegration): HttpClient = createGeneric()
}

@OptIn(ExperimentalCoroutinesApi::class)
class MediaServerLibraryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private var activeVm: MediaServerLibraryViewModel? = null
    private var activeManagerScope: CoroutineScope? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        activeVm?.viewModelScope?.cancel()
        activeManagerScope?.cancel()
    }

    private val plexSectionsJson = """
        {
            "MediaContainer": {
                "size": 2,
                "Directory": [
                    { "key": "1", "title": "Movies", "type": "movie" },
                    { "key": "2", "title": "TV Shows", "type": "show" }
                ]
            }
        }
    """.trimIndent()

    private val plexMoviesJson = """
        {
            "MediaContainer": {
                "size": 2,
                "Metadata": [
                    {
                        "ratingKey": "101",
                        "title": "Inception",
                        "type": "movie",
                        "year": 2010,
                        "summary": "Dream within a dream",
                        "rating": 8.8,
                        "viewCount": 1
                    },
                    {
                        "ratingKey": "102",
                        "title": "Interstellar",
                        "type": "movie",
                        "year": 2014,
                        "summary": "Space exploration",
                        "rating": 8.6,
                        "viewCount": 0
                    }
                ]
            }
        }
    """.trimIndent()

    private val fakeIntegration = MediaServerIntegration(
        id = 1,
        serverType = MediaServerType.Plex,
        label = "My Plex",
        url = "http://192.168.1.100:32400",
        token = EncryptedString("test-token"),
        clientIdentifier = "test-client-id",
        enabled = true,
    )

    private fun createSetup(): Pair<MediaServerLibraryViewModel, MediaServerIntegration> {
        val dao = FakeLibraryMediaServerDao(listOf(fakeIntegration))
        val mockEngine = MockEngine { request ->
            val path = request.url.encodedPath
            when {
                path.contains("/library/sections/1/all") -> respond(
                    plexMoviesJson,
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json"),
                )
                path.contains("/library/sections") -> respond(
                    plexSectionsJson,
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, "application/json"),
                )
                else -> respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }

        val json = Json { ignoreUnknownKeys = true }
        val logger = object : Logger {
            override fun log(message: String) {}
        }
        val clientFactory = TestLibraryHttpClientFactory(json, mockEngine, logger)
        val managerScope = CoroutineScope(Dispatchers.Default + Job())
        activeManagerScope = managerScope
        val manager = MediaServerManager(dao, clientFactory, managerScope)

        val vm = MediaServerLibraryViewModel(manager)
        activeVm = vm
        return vm to fakeIntegration
    }

    private suspend fun awaitLoaded(vm: MediaServerLibraryViewModel) {
        var attempts = 0
        while (attempts < 50 && (vm.isLoading.value || vm.libraries.value.isEmpty() || vm.filteredItems.value.isEmpty())) {
            delay(50)
            attempts++
        }
    }

    @Test
    fun loadsConfiguredServersAndInitializes() = runBlocking {
        val (vm, integration) = createSetup()
        vm.selectServer(integration)
        awaitLoaded(vm)

        assertNotNull(vm.selectedServer.value)
        assertEquals("My Plex", vm.selectedServer.value?.label)

        assertEquals(2, vm.libraries.value.size)
        assertEquals("Movies", vm.selectedLibrary.value?.title)

        assertEquals(2, vm.filteredItems.value.size)
        assertEquals("Inception", vm.filteredItems.value[0].title)
        assertEquals("Interstellar", vm.filteredItems.value[1].title)
    }

    @Test
    fun searchAndWatchedFiltersWork() = runBlocking {
        val (vm, integration) = createSetup()
        vm.selectServer(integration)
        awaitLoaded(vm)

        // Test search query
        vm.setSearchQuery("Inception")
        delay(50)
        assertEquals(1, vm.filteredItems.value.size)
        assertEquals("Inception", vm.filteredItems.value.first().title)

        vm.setSearchQuery("")
        delay(50)
        assertEquals(2, vm.filteredItems.value.size)

        // Test Watched filter (Inception has viewCount=1, Interstellar viewCount=0)
        vm.setWatchedFilter(WatchedFilter.WATCHED)
        delay(50)
        assertEquals(1, vm.filteredItems.value.size)
        assertEquals("Inception", vm.filteredItems.value.first().title)

        vm.setWatchedFilter(WatchedFilter.UNWATCHED)
        delay(50)
        assertEquals(1, vm.filteredItems.value.size)
        assertEquals("Interstellar", vm.filteredItems.value.first().title)
    }

    @Test
    fun sortingAndTogglingViewType() = runBlocking {
        val (vm, integration) = createSetup()
        vm.selectServer(integration)
        awaitLoaded(vm)

        // Sort by Year Desc
        vm.setSortBy(SortBy.Year)
        vm.setSortOrder(SortOrder.Desc)
        delay(50)

        assertEquals("Interstellar", vm.filteredItems.value[0].title) // 2014
        assertEquals("Inception", vm.filteredItems.value[1].title) // 2010

        // Toggle View Type
        assertEquals(ViewType.Grid, vm.viewType.value)
        vm.toggleViewType()
        assertEquals(ViewType.List, vm.viewType.value)
        vm.toggleViewType()
        assertEquals(ViewType.Grid, vm.viewType.value)
    }

    @Test
    fun switchingServerRestoresLibraryAndDoesNotShowLoadingWhenCached() = runBlocking {
        val (vm, integration) = createSetup()
        vm.selectServer(integration)
        awaitLoaded(vm)

        assertEquals("Movies", vm.selectedLibrary.value?.title)
        assertFalse(vm.isLoading.value)
        assertEquals(2, vm.filteredItems.value.size)

        // Re-selecting the server should use cached data without setting isLoading = true
        vm.selectServer(integration)
        assertFalse(vm.isLoading.value)
        assertEquals("Movies", vm.selectedLibrary.value?.title)
        assertEquals(2, vm.filteredItems.value.size)
    }
}
