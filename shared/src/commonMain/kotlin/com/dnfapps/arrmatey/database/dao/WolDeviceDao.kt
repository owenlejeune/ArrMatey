package com.dnfapps.arrmatey.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dnfapps.arrmatey.wol.model.WolDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface WolDeviceDao {
    @Query("SELECT * FROM wol_devices ORDER BY id ASC")
    fun getAllWolDevices(): Flow<List<WolDevice>>

    @Query("SELECT * FROM wol_devices ORDER BY id ASC")
    suspend fun getAllWolDevicesList(): List<WolDevice>

    @Query("SELECT * FROM wol_devices WHERE id = :id")
    suspend fun getWolDeviceById(id: Long): WolDevice?

    @Query("SELECT * FROM wol_devices WHERE id = :id")
    fun observeWolDeviceById(id: Long): Flow<WolDevice?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: WolDevice): Long

    @Update
    suspend fun update(device: WolDevice): Int

    @Delete
    suspend fun delete(device: WolDevice)

    @Query("DELETE FROM wol_devices WHERE id = :id")
    suspend fun deleteById(id: Long)
}
