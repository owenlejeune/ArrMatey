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
    @Query("SELECT * FROM custom_webpages ORDER BY position ASC")
    fun getAllWebpages(): Flow<List<CustomWebpage>>

    @Query("SELECT * FROM custom_webpages WHERE id = :id")
    suspend fun getWebpageById(id: Long): CustomWebpage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(webpage: CustomWebpage): Long

    @Update
    suspend fun update(webpage: CustomWebpage)

    @Delete
    suspend fun delete(webpage: CustomWebpage)

    @Query("DELETE FROM custom_webpages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE custom_webpages SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Int)

    @Query("SELECT MAX(position) FROM custom_webpages")
    suspend fun getMaxPosition(): Int?
}