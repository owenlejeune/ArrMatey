package com.dnfapps.arrmatey.arr.usecase

import app.cash.turbine.test
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.networking.OperationStatus
import dev.shivathapaa.logger.api.LoggerFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DeleteMediaUseCaseTest {

    private val fakeInstance = Instance(
        id = 1,
        label = "Test Sonarr",
        url = "http://localhost:8989",
        apiKey = EncryptedString("test-api-key"),
        type = InstanceType.Sonarr,
        enabled = true
    )
    private val fakeLogger = LoggerFactory.get("test")

    @Test
    fun testDeleteMediaSuccess() = runTest {
        val mockEngine = MockEngine { _ ->
            respond("", HttpStatusCode.OK)
        }
        val httpClient = HttpClient(mockEngine)
        val repository = ArrInstanceRepository(fakeInstance, httpClient, fakeLogger)
        val useCase = DeleteMediaUseCase()

        useCase(123, true, false, repository).test {
            assertTrue(awaitItem() is OperationStatus.InProgress)
            assertTrue(awaitItem() is OperationStatus.Success)
            awaitComplete()
        }
    }
}
