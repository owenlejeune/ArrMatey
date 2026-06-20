package com.dnfapps.arrmatey.backup.usecase

import com.dnfapps.arrmatey.backup.TransportEncryptor
import com.dnfapps.arrmatey.backup.model.BackupExport
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.instances.model.Instance
import kotlinx.serialization.json.Json

class ImportDataUseCase(
    private val instanceDao: InstanceDao,
    private val downloadClientDao: DownloadClientDao,
    private val instancePreferenceStoreRepository: InstancePreferenceStoreRepository,
    private val transportEncryptor: TransportEncryptor,
    private val json: Json
) {
    fun decryptBackup(encryptedData: String, password: String): BackupExport {
        val jsonString = transportEncryptor.decrypt(encryptedData, password)
        return json.decodeFromString(jsonString)
    }

    suspend fun importSelected(
        backup: BackupExport,
        selectedInstanceIndices: Set<Int>,
        selectedDownloadClientIndices: Set<Int>
    ) {
        backup.instances.forEachIndexed { index, export ->
            if (index in selectedInstanceIndices) {
                val instance = Instance(
                    type = export.type,
                    label = export.label,
                    url = export.url,
                    apiKey = EncryptedString(export.apiKey),
                    noApiKeyRequired = export.noApiKeyRequired,
                    enabled = export.enabled,
                    slowInstance = export.slowInstance,
                    customTimeout = export.customTimeout,
                    notificationsEnabled = export.notificationsEnabled,
                    headers = export.headers,
                    localNetworkEnabled = export.localNetworkEnabled,
                    localNetworkSsids = export.localNetworkSsids,
                    localNetworkEndpoint = export.localNetworkEndpoint
                )
                
                // Handling conflicts by using a unique label/url check if needed,
                // but for now let's just try to insert. 
                // The Dao might need an UPSERT or we handle it here.
                val existingByUrl = instanceDao.findByUrl(instance.url)
                val existingByLabel = instanceDao.findByLabel(instance.label)
                
                val finalInstance = if (existingByUrl != null) {
                    instance.copy(id = existingByUrl)
                } else if (existingByLabel != null) {
                    instance.copy(id = existingByLabel)
                } else {
                    instance
                }

                val id = if (finalInstance.id != 0L) {
                    instanceDao.update(finalInstance)
                    finalInstance.id
                } else {
                    instanceDao.insert(finalInstance)
                }

                if (id > 0 && export.preferences != null) {
                    val prefStore = instancePreferenceStoreRepository.getInstancePreferences(id)
                    prefStore.savePreferences(export.preferences)
                }
            }
        }

        backup.downloadClients.forEachIndexed { index, export ->
            if (index in selectedDownloadClientIndices) {
                val client = DownloadClient(
                    type = export.type,
                    label = export.label,
                    url = export.url,
                    username = EncryptedString(export.username),
                    password = EncryptedString(export.password),
                    apiKey = EncryptedString(export.apiKey),
                    noApiKeyRequired = export.noApiKeyRequired,
                    headers = export.headers,
                    localNetworkEnabled = export.localNetworkEnabled,
                    localNetworkSsids = export.localNetworkSsids,
                    localNetworkEndpoint = export.localNetworkEndpoint
                )

                val existingByUrl = downloadClientDao.findByUrl(client.url)
                val existingByLabel = downloadClientDao.findByLabel(client.label)

                val finalClient = if (existingByUrl != null) {
                    client.copy(id = existingByUrl)
                } else if (existingByLabel != null) {
                    client.copy(id = existingByLabel)
                } else {
                    client
                }

                if (finalClient.id != 0L) {
                    downloadClientDao.update(finalClient)
                } else {
                    downloadClientDao.insert(finalClient)
                }
            }
        }
    }
}
