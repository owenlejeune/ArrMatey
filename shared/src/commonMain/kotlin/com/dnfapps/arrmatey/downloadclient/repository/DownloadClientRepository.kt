package com.dnfapps.arrmatey.downloadclient.repository

import com.dnfapps.arrmatey.downloadclient.database.DownloadClientConflictField
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientInsertResult
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class DownloadClientRepository(
    private val downloadClientDao: DownloadClientDao
) {

    val allDownloadClientsFlow: StateFlow<List<DownloadClient>> = downloadClientDao.observeAllDownloadClients()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun observeAllDownloadClients(): Flow<List<DownloadClient>> =
        downloadClientDao.observeAllDownloadClients()

    fun observeSelectedDownloadClient(): Flow<DownloadClient?> =
        downloadClientDao.observeSelectedDownloadClient()

    suspend fun getDownloadClientById(id: Long): DownloadClient? =
        downloadClientDao.getDownloadClientById(id)

    suspend fun getSelectedDownloadClient(): DownloadClient? =
        downloadClientDao.getAllDownloadClients().firstOrNull { it.selected }

    suspend fun getAllDownloadClients(): List<DownloadClient> =
        downloadClientDao.getAllDownloadClients()

    suspend fun createDownloadClient(downloadClient: DownloadClient): DownloadClientInsertResult {
        return try {
            val urlConflict = downloadClientDao.findByUrl(downloadClient.url) != null
            val labelConflict = downloadClientDao.findByLabel(downloadClient.label) != null

            val conflictFields = buildList {
                if (urlConflict) add(DownloadClientConflictField.DownloadClientUrl)
                if (labelConflict) add(DownloadClientConflictField.DownloadClientLabel)
            }

            if (conflictFields.isNotEmpty()) {
                DownloadClientInsertResult.Conflict(fields = conflictFields)
            } else {
                val currentClients = downloadClientDao.getAllDownloadClients()
                val shouldBeSelected = currentClients.none { it.selected }
                val id = downloadClientDao.insert(downloadClient.copy(selected = shouldBeSelected))
                if (id > 0L) DownloadClientInsertResult.Success(id)
                else DownloadClientInsertResult.Error("Failed to save")
            }
        } catch (e: Exception) {
            DownloadClientInsertResult.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun updateDownloadClient(downloadClient: DownloadClient): DownloadClientInsertResult {
        return try {
            val urlConflict = downloadClientDao.findOtherByUrl(downloadClient.url, downloadClient.id) != null
            val labelConflict = downloadClientDao.findOtherByLabel(downloadClient.label, downloadClient.id) != null

            val conflictFields = buildList {
                if (urlConflict) add(DownloadClientConflictField.DownloadClientUrl)
                if (labelConflict) add(DownloadClientConflictField.DownloadClientLabel)
            }

            if (conflictFields.isNotEmpty()) {
                DownloadClientInsertResult.Conflict(fields = conflictFields)
            } else {
                val rows = downloadClientDao.update(downloadClient)
                if (rows > 0) DownloadClientInsertResult.Success(downloadClient.id)
                else DownloadClientInsertResult.Error("Failed to update")
            }
        } catch (e: Exception) {
            DownloadClientInsertResult.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun deleteDownloadClient(downloadClient: DownloadClient) {
        downloadClientDao.deleteAndUpdateSelected(downloadClient)
    }

    suspend fun setDownloadClientActive(downloadClient: DownloadClient) {
        withContext(Dispatchers.IO) {
            downloadClientDao.setDownloadClientAsSelected(downloadClient.id)
        }
    }
}
