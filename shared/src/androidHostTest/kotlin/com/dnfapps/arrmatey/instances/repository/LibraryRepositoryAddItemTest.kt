package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ArrClient
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.networking.NetworkResult
import dev.shivathapaa.logger.api.LoggerFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class LibraryRepositoryAddItemTest {
    private val lookupItem = mockk<ArrSeries>()
    private val client =
        mockk<ArrClient> {
            coEvery { getLibrary() } returns NetworkResult.Success(emptyList<ArrMedia>())
        }
    private val repository = LibraryRepository(client, LoggerFactory.get("test"), mockk(relaxed = true))

    @Test
    fun addItem_returnsAddedItemOnSuccess() = runTest {
        val addedItem = mockk<ArrSeries> { every { id } returns 42L }
        coEvery { client.addItemToLibrary(lookupItem) } returns NetworkResult.Success(addedItem)

        assertSame(addedItem, repository.addItem(lookupItem, searchOnAdd = false))
    }

    @Test
    fun addItem_returnsNullOnError() = runTest {
        coEvery { client.addItemToLibrary(lookupItem) } returns NetworkResult.Error(message = "boom")

        assertNull(repository.addItem(lookupItem, searchOnAdd = false))
    }
}
