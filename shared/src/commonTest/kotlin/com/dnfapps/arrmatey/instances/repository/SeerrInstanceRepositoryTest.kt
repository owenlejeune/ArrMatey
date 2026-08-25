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

class SeerrInstanceRepositoryTest {

    private val fakeInstance = Instance(
        id = 1,
        label = "Test Seerr",
        url = "http://localhost:5055",
        apiKey = EncryptedString("test-api-key"),
        type = InstanceType.Seerr,
        enabled = true
    )

    @Test
    fun testGetLoggedInUser() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"id": 1, "displayName": "Admin", "email": "admin@test.com", "permissions": 0, "userType": 1}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        val repository = SeerrInstanceRepository(fakeInstance, httpClient)

        repository.getLoggedInUser()

        assertNotNull(repository.loggedInUser.value)
        assertEquals(1, repository.loggedInUser.value?.id)
        assertEquals("Admin", repository.loggedInUser.value?.displayName)
    }
}
