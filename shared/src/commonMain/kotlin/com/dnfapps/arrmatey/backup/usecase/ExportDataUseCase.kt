package com.dnfapps.arrmatey.backup.usecase

import com.dnfapps.arrmatey.backup.TransportEncryptor
import com.dnfapps.arrmatey.backup.model.BackupExport
import com.dnfapps.arrmatey.backup.model.DownloadClientExport
import com.dnfapps.arrmatey.backup.model.InstanceExport
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ExportDataUseCase(
    private val instanceDao: InstanceDao,
    private val downloadClientDao: DownloadClientDao,
    private val instancePreferenceStoreRepository: InstancePreferenceStoreRepository,
    private val transportEncryptor: TransportEncryptor,
    private val json: Json
) {
    suspend operator fun invoke(
        password: String,
        selectedInstanceIds: Set<Long>,
        selectedDownloadClientIds: Set<Long>,
        includePreferences: Boolean
    ): String {
        val instances = instanceDao.getAllInstances()
            .filter { it.id in selectedInstanceIds }
        
        val instanceExports = instances.map { instance ->
            val preferences = if (includePreferences) {
                instancePreferenceStoreRepository.getInstancePreferences(instance.id)
                    .observePreferences().first()
            } else null
            
            InstanceExport(
                type = instance.type,
                label = instance.label,
                url = instance.url,
                apiKey = instance.apiKey.value,
                noApiKeyRequired = instance.noApiKeyRequired,
                enabled = instance.enabled,
                slowInstance = instance.slowInstance,
                customTimeout = instance.customTimeout,
                notificationsEnabled = instance.notificationsEnabled,
                headers = instance.headers,
                localNetworkEnabled = instance.localNetworkEnabled,
                localNetworkSsids = instance.localNetworkSsids,
                localNetworkEndpoint = instance.localNetworkEndpoint,
                preferences = preferences
            )
        }

        val downloadClients = downloadClientDao.getAllDownloadClients()
            .filter { it.id in selectedDownloadClientIds }
            
        val downloadClientExports = downloadClients.map { client ->
            DownloadClientExport(
                type = client.type,
                label = client.label,
                url = client.url,
                username = client.username.value,
                password = client.password.value,
                apiKey = client.apiKey.value,
                noApiKeyRequired = client.noApiKeyRequired,
                headers = client.headers,
                localNetworkEnabled = client.localNetworkEnabled,
                localNetworkSsids = client.localNetworkSsids,
                localNetworkEndpoint = client.localNetworkEndpoint
            )
        }

        val backup = BackupExport(
            instances = instanceExports,
            downloadClients = downloadClientExports
        )

        val jsonString = json.encodeToString(backup)
        return transportEncryptor.encrypt(jsonString, password)
    }
}
