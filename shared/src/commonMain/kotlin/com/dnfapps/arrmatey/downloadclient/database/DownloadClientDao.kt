package com.dnfapps.arrmatey.downloadclient.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadClientDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(downloadClient: DownloadClient): Long

    @Delete
    suspend fun delete(downloadClient: DownloadClient)

    @Update
    suspend fun update(downloadClient: DownloadClient): Int

    @Query("SELECT * FROM download_clients")
    fun observeAllDownloadClients(): Flow<List<DownloadClient>>

    @Query("SELECT * FROM download_clients WHERE selected = 1 LIMIT 1")
    fun observeSelectedDownloadClient(): Flow<DownloadClient?>

    @Query("SELECT * FROM download_clients WHERE id = :id")
    suspend fun getDownloadClientById(id: Long): DownloadClient?

    @Query("SELECT * FROM download_clients")
    suspend fun getAllDownloadClients(): List<DownloadClient>

    @Query("SELECT id FROM download_clients WHERE url = :url")
    suspend fun findByUrl(url: String): Long?

    @Query("SELECT id FROM download_clients WHERE label = :label")
    suspend fun findByLabel(label: String): Long?

    @Query("SELECT id FROM download_clients WHERE url = :url AND id != :currentId LIMIT 1")
    suspend fun findOtherByUrl(url: String, currentId: Long): Long?

    @Query("SELECT id FROM download_clients WHERE label = :label AND id != :currentId LIMIT 1")
    suspend fun findOtherByLabel(label: String, currentId: Long): Long?

    @Query("UPDATE download_clients SET selected = 0")
    suspend fun unselectAll()

    @Query("UPDATE download_clients SET selected = 1 WHERE id = :id")
    suspend fun selectDownloadClient(id: Long)

    @Transaction
    suspend fun setDownloadClientAsSelected(id: Long) {
        unselectAll()
        selectDownloadClient(id)
    }

    @Query(
        """
        UPDATE download_clients
        SET selected = true
        WHERE id = (
            SELECT id
            FROM download_clients
            WHERE NOT EXISTS (
                SELECT 1
                FROM download_clients
                WHERE selected = true
            )
            ORDER BY id
            LIMIT 1
        )
    """
    )
    suspend fun ensureFirstSelectedIfNone()

    @Transaction
    suspend fun deleteAndUpdateSelected(downloadClient: DownloadClient) {
        delete(downloadClient)
        ensureFirstSelectedIfNone()
    }
}
