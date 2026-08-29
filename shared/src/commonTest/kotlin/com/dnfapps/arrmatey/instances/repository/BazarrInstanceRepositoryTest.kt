package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
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

class BazarrInstanceRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 1,
            label = "Test Bazarr",
            url = "http://localhost:6767",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Bazarr,
            enabled = true,
        )

    @Test
    fun testGetSystemStatus() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content =
                            """
                            {
                              "data": {
                                "bazarr_version": "1.4.3",
                                "package_version": "1.4.3",
                                "sonarr_version": "",
                                "radarr_version": "",
                                "operating_system": "",
                                "python_version": "",
                                "database_engine": "",
                                "database_migration": "",
                                "bazarr_directory": "",
                                "bazarr_config_directory": "",
                                "start_time": 0.0,
                                "timezone": "",
                                "cpu_cores": 1
                              }
                            }
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
            val repository = BazarrInstanceRepository(fakeInstance, httpClient)

            repository.getSystemStatus()

            assertNotNull(repository.systemStatus.value)
            assertEquals(
                "1.4.3",
                repository.systemStatus.value
                    ?.data
                    ?.bazarr_version,
            )
        }

    @Test
    fun testRefreshBadges() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{"episodes": 10, "movies": 5, "providers": 2}""",
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
            val repository = BazarrInstanceRepository(fakeInstance, httpClient)

            repository.refreshBadges()

            assertEquals(10, repository.wantedEpisodesCount.value)
            assertEquals(5, repository.wantedMoviesCount.value)
            assertEquals(2, repository.providerIssuesCount.value)
        }
}
