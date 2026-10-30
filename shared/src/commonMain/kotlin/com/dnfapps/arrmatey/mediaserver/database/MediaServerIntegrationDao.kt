package com.dnfapps.arrmatey.mediaserver.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dnfapps.arrmatey.mediaserver.model.MediaServerIntegration
import com.dnfapps.arrmatey.mediaserver.model.MediaServerType
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaServerIntegrationDao {
    @Query("SELECT * FROM media_server_integrations ORDER BY id ASC")
    fun observeAll(): Flow<List<MediaServerIntegration>>

    @Query("SELECT * FROM media_server_integrations WHERE serverType = :type LIMIT 1")
    fun observeByType(type: MediaServerType): Flow<MediaServerIntegration?>

    @Query("SELECT * FROM media_server_integrations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MediaServerIntegration?

    @Query("SELECT * FROM media_server_integrations WHERE serverType = :type LIMIT 1")
    suspend fun getByType(type: MediaServerType): MediaServerIntegration?

    @Query("SELECT * FROM media_server_integrations")
    suspend fun getAll(): List<MediaServerIntegration>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(integration: MediaServerIntegration): Long

    @Update
    suspend fun update(integration: MediaServerIntegration)

    @Delete
    suspend fun delete(integration: MediaServerIntegration)

    @Query("DELETE FROM media_server_integrations WHERE serverType = :type")
    suspend fun deleteByType(type: MediaServerType)
}
