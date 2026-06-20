package com.dnfapps.arrmatey.database

import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import kotlinx.coroutines.flow.first

class CredentialMigrationUseCase(
    private val instanceDao: InstanceDao,
    private val downloadClientDao: DownloadClientDao,
    private val preferencesStore: PreferencesStore
) {
    suspend operator fun invoke() {
        val migrated = preferencesStore.credentialsMigrated.first()
        if (migrated) return

        val instances = instanceDao.getAllInstances()
        if (instances.isNotEmpty()) {
            instanceDao.updateAll(instances)
        }

        val downloadClients = downloadClientDao.getAllDownloadClients()
        if (downloadClients.isNotEmpty()) {
            downloadClientDao.updateAll(downloadClients)
        }

        preferencesStore.markCredentialsMigrated()
    }
}
