package com.dnfapps.arrmatey.wol.repository

import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.database.dao.WolDeviceDao
import com.dnfapps.arrmatey.wol.model.WolDevice
import kotlinx.coroutines.flow.Flow

class WolDeviceRepository(
    private val dao: WolDeviceDao,
) {
    fun getAllWolDevices(): Flow<List<WolDevice>> = dao.getAllWolDevices()

    suspend fun getWolDeviceById(id: Long): WolDevice? = dao.getWolDeviceById(id)

    fun observeWolDeviceById(id: Long): Flow<WolDevice?> = dao.observeWolDeviceById(id)

    suspend fun addWolDevice(device: WolDevice): InsertResult = try {
        val id = dao.insert(device)
        if (id > 0L) {
            InsertResult.Success(id)
        } else {
            InsertResult.Error("Failed to save WoL device")
        }
    } catch (e: Exception) {
        InsertResult.Error(e.message ?: "An error occurred")
    }

    suspend fun updateWolDevice(device: WolDevice): InsertResult = try {
        val rows = dao.update(device)
        if (rows > 0) {
            InsertResult.Success(device.id)
        } else {
            InsertResult.Error("Failed to update WoL device")
        }
    } catch (e: Exception) {
        InsertResult.Error(e.message ?: "An error occurred")
    }

    suspend fun deleteWolDevice(device: WolDevice) {
        dao.delete(device)
    }

    suspend fun deleteWolDeviceById(id: Long) {
        dao.deleteById(id)
    }
}
