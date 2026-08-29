package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProwlarrInstanceRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 1,
            label = "Test Prowlarr",
            url = "http://localhost:9696",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Prowlarr,
            enabled = true,
        )

    @Test
    fun testRefreshStatus() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{"version": "1.2.0"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                            },
                        )
                    }
                }
            val repository = ProwlarrInstanceRepository(fakeInstance, httpClient)

            repository.refreshStatus()

            assertNotNull(repository.softwareStatus.value)
            assertEquals("1.2.0", repository.softwareStatus.value?.version)
        }

    @Test
    fun testGetIndexers() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content =
                            """
                            [
                              {
                                "id": 1,
                                "name": "Indexer 1",
                                "enable": true,
                                "supportsRss": true,
                                "supportsSearch": true,
                                "supportsRedirect": true,
                                "priority": 1
                              }
                            ]
                            """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", "application/json"),
                    )
                }
            val httpClient =
                HttpClient(mockEngine) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                ignoreUnknownKeys = true
                            },
                        )
                    }
                }
            val repository = ProwlarrInstanceRepository(fakeInstance, httpClient)

            val result = repository.getIndexers()

            assertTrue(result is NetworkResult.Success)
            assertEquals(1, result.data.size)
            assertEquals("Indexer 1", result.data[0].name)
            assertEquals(1, repository.indexers.value.size)
        }
}
