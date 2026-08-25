package com.dnfapps.networking

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpClientExtensionsTest {

    @Test
    fun testSafeGetSuccess() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"id": 1}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString())
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val result = httpClient.safeGet<TestModel>("http://test.com")
        
        if (result is NetworkResult.Error) {
            println("Error: ${result.message}, code: ${result.code}, cause: ${result.cause}")
        }
        assertTrue(result is NetworkResult.Success, "Expected Success but got $result")
        assertEquals(1, result.data.id)
    }

    @Test
    fun testSafeGetError() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound,
                headers = headersOf("Content-Type", ContentType.Text.Plain.toString())
            )
        }
        // Need to enable expectSuccess to get ClientRequestException
        val httpClient = HttpClient(mockEngine) {
            expectSuccess = true
        }

        val result = httpClient.safeGet<TestModel>("http://test.com")
        
        assertTrue(result is NetworkResult.Error, "Expected Error but got $result")
        assertEquals(404, result.code)
    }

    @kotlinx.serialization.Serializable
    data class TestModel(val id: Int)
}
