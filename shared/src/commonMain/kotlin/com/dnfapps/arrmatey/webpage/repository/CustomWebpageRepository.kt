package com.dnfapps.arrmatey.webpage.repository

import com.dnfapps.arrmatey.database.dao.CustomWebpageDao
import com.dnfapps.arrmatey.database.dao.InsertResult
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

    suspend fun addWebpage(webpage: CustomWebpage): InsertResult {
        return try {
            val id = dao.insert(webpage)
            if (id > 0L) InsertResult.Success(id)
            else InsertResult.Error("Failed to save webpage")
        } catch (e: Exception) {
            InsertResult.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun updateWebpage(webpage: CustomWebpage): InsertResult {
        return try {
            val rows = dao.update(webpage)
            if (rows > 0) InsertResult.Success(webpage.id)
            else InsertResult.Error("Failed up update webpage")
        } catch (e: Exception) {
            InsertResult.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun deleteWebpage(webpage: CustomWebpage) {
        dao.delete(webpage)
    }

    suspend fun deleteWebpageById(id: Long) {
        dao.deleteById(id)
    }
}