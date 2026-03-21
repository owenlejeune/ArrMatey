package com.dnfapps.arrmatey.webpage.repository

import com.dnfapps.arrmatey.database.dao.CustomWebpageDao
import com.dnfapps.arrmatey.webpage.model.CustomWebpage
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class CustomWebpageRepository(
    private val dao: CustomWebpageDao
) {
    fun getAllWebpages(): Flow<List<CustomWebpage>> {
        return dao.getAllWebpages()
    }

    suspend fun getWebpageById(id: Long): CustomWebpage? {
        return dao.getWebpageById(id)
    }

    suspend fun addWebpage(webpage: CustomWebpage): Long {
        val maxPosition = dao.getMaxPosition() ?: -1
        val entity = webpage.copy(position = maxPosition + 1)
        return dao.insert(entity)
    }

    suspend fun updateWebpage(webpage: CustomWebpage) {
        dao.update(webpage)
    }

    suspend fun deleteWebpage(webpage: CustomWebpage) {
        dao.delete(webpage)
    }

    suspend fun deleteWebpageById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun updatePositions(webpages: List<CustomWebpage>) {
        webpages.forEachIndexed { index, webpage ->
            dao.updatePosition(webpage.id, index)
        }
    }
}