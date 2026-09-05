package com.dnfapps.arrmatey.arr.usecase

import app.cash.turbine.test
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.model.OperationStatus
import dev.shivathapaa.logger.api.LoggerFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteBookFilesUseCaseTest {
    private val logger = LoggerFactory.get("test")

    private fun instance(type: InstanceType) =
        Instance(
            id = 1,
            label = "Test",
            url = "http://localhost",
            apiKey = EncryptedString("k"),
            type = type,
            enabled = true,
        )

    @Test
    fun testWrongInstanceTypeEmitsError() =
        runTest {
            val mockEngine = MockEngine { respond("", HttpStatusCode.OK) }
            val repository = ArrInstanceRepository(instance(InstanceType.Sonarr), HttpClient(mockEngine), logger)
            val useCase = DeleteBookFilesUseCase()

            useCase(bookFileIds = listOf(1, 2, 3), repository = repository).test {
                assertTrue(awaitItem() is OperationStatus.InProgress)
                val error = awaitItem()
                assertTrue(error is OperationStatus.Error)
                assertEquals("Not a Readarr instance", error.message)
                awaitComplete()
            }
        }
}
