package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.SonarrClient
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import dev.shivathapaa.logger.api.LoggerFactory
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

class MetadataRepositoryTest {
    private val fakeInstance =
        Instance(
            id = 1,
            label = "Test Sonarr",
            url = "http://localhost:8989",
            apiKey = EncryptedString("test-api-key"),
            type = InstanceType.Sonarr,
            enabled = true,
        )

    private val fakeLogger = LoggerFactory.get("test")

    @Test
    fun testRefreshStatus() =
        runTest {
            val mockEngine =
                MockEngine { _ ->
                    respond(
                        content = """{"version": "4.0.0.648"}""",
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
            val client = SonarrClient(fakeInstance, httpClient)
            val metadataRepo = MetadataRepository(client, fakeLogger)

            metadataRepo.refreshStatus()

            assertNotNull(metadataRepo.softwareStatus.value)
            assertEquals("4.0.0.648", metadataRepo.softwareStatus.value?.version)
        }
}
