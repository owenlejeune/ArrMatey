package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import dev.shivathapaa.logger.api.LoggerFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class PerformRefreshUseCaseTest {
    private val logger = LoggerFactory.get("test")
    private val useCase = PerformRefreshUseCase()

    private fun repository(): ArrInstanceRepository {
        val instance =
            Instance(
                id = 1,
                label = "Test",
                url = "http://localhost",
                apiKey = EncryptedString("k"),
                type = InstanceType.Sonarr,
                enabled = true,
            )
        val mockEngine = MockEngine { respond("", HttpStatusCode.OK) }
        return ArrInstanceRepository(instance, HttpClient(mockEngine), logger)
    }

    @Test
    fun testUnsupportedTypeThrows() =
        runTest {
            assertFailsWith<UnsupportedOperationException> {
                useCase(mediaId = 1, type = InstanceType.Seerr, repository = repository())
            }
        }

    @Test
    fun testBulkRefreshUnsupportedTypeThrows() =
        runTest {
            assertFailsWith<UnsupportedOperationException> {
                useCase.bulkRefresh(ids = listOf(1, 2), type = InstanceType.Bazarr, repository = repository())
            }
        }

    @Test
    fun testProwlarrRefreshThrows() =
        runTest {
            assertFailsWith<UnsupportedOperationException> {
                useCase(mediaId = 1, type = InstanceType.Prowlarr, repository = repository())
            }
        }
}
