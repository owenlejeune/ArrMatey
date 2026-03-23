package com.dnfapps.arrmatey.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dnfapps.arrmatey.webpage.model.CustomWebpage
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomWebpageDao {
    @Query("SELECT * FROM custom_webpages ORDER BY id ASC")
    fun getAllWebpages(): Flow<List<CustomWebpage>>

    @Query("SELECT * FROM custom_webpages WHERE id = :id")
    suspend fun getWebpageById(id: Long): CustomWebpage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(webpage: CustomWebpage): Long

    @Update
    suspend fun update(webpage: CustomWebpage): Int

    @Delete
    suspend fun delete(webpage: CustomWebpage)

    @Query("DELETE FROM custom_webpages WHERE id = :id")
    suspend fun deleteById(id: Long)
}