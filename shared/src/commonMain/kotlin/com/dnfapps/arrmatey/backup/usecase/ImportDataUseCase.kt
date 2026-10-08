package com.dnfapps.arrmatey.backup.usecase

import com.dnfapps.arrmatey.backup.TransportEncryptor
import com.dnfapps.arrmatey.backup.model.BackupExport
import com.dnfapps.arrmatey.database.EncryptedString
import com.dnfapps.arrmatey.database.dao.CustomWebpageDao
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import com.dnfapps.arrmatey.downloadclient.model.DownloadClient
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.webpage.model.CustomWebpage
import kotlinx.serialization.json.Json

class ImportDataUseCase(
    private val instanceDao: InstanceDao,
    private val downloadClientDao: DownloadClientDao,
    private val customWebpageDao: CustomWebpageDao,
    private val instancePreferenceStoreRepository: InstancePreferenceStoreRepository,
    private val preferencesStore: PreferencesStore,
    private val transportEncryptor: TransportEncryptor,
    private val json: Json,
) {
    fun decryptBackup(
        encryptedData: String,
        password: String,
    ): BackupExport {
        val jsonString = transportEncryptor.decrypt(encryptedData, password)
        val sanitizedJson = sanitizeLegacyBackupJson(jsonString)
        return json.decodeFromString(sanitizedJson)
    }

    private fun sanitizeLegacyBackupJson(jsonString: String): String = jsonString
        .replace("\"Booksehlf\"", "\"Bookshelf\"")
        .replace("\"Booksehelf\"", "\"Bookshelf\"")
        .replace("\"booksehlf\"", "\"bookshelf\"")
        .replace("\"booksehelf\"", "\"bookshelf\"")

    suspend fun importSelected(
        backup: BackupExport,
        selectedInstanceIndices: Set<Int>,
        selectedDownloadClientIndices: Set<Int>,
        selectedCustomWebpageIndices: Set<Int> = emptySet(),
        importTabPreferences: Boolean,
        importUiPreferences: Boolean,
        importIntegrationsPreferences: Boolean = true,
    ) {
        backup.instances.forEachIndexed { index, export ->
            if (index in selectedInstanceIndices) {
                val existingByUrl = instanceDao.findByUrl(export.url)
                val existingByLabel = instanceDao.findByLabel(export.label)
                val existingInstanceId = existingByUrl ?: existingByLabel
                val existingInstance = existingInstanceId?.let { instanceDao.getInstanceById(it) }

                var uniqueLabel = export.label
                if (existingByUrl != null && existingByLabel != null && existingByLabel != existingByUrl) {
                    uniqueLabel = "${export.label} (Imported)"
                }

                val currentInstancesOfSameType = instanceDao.getInstancesOfType(export.type)
                val shouldBeSelected = existingInstance?.selected
                    ?: currentInstancesOfSameType.none { it.selected }

                val instance =
                    Instance(
                        id = existingInstanceId ?: 0L,
                        type = export.type,
                        label = uniqueLabel,
                        url = export.url,
                        apiKey = EncryptedString(export.apiKey),
                        noApiKeyRequired = export.noApiKeyRequired,
                        enabled = export.enabled,
                        slowInstance = export.slowInstance,
                        customTimeout = export.customTimeout,
                        selected = shouldBeSelected,
                        notificationsEnabled = export.notificationsEnabled,
                        headers = export.headers,
                        localNetworkEnabled = export.localNetworkEnabled,
                        localNetworkSsids = export.localNetworkSsids,
                        localNetworkEndpoint = export.localNetworkEndpoint,
                    )

                val id =
                    if (instance.id != 0L) {
                        instanceDao.update(instance)
                        instance.id
                    } else {
                        instanceDao.insert(instance)
                    }

                if (id > 0 && export.preferences != null) {
                    val prefStore = instancePreferenceStoreRepository.getInstancePreferences(id)
                    prefStore.savePreferences(export.preferences)
                }
            }
        }

        InstanceType.entries.forEach { type ->
            instanceDao.ensureFirstSelectedIfNone(type)
        }

        backup.downloadClients.forEachIndexed { index, export ->
            if (index in selectedDownloadClientIndices) {
                val existingByUrl = downloadClientDao.findByUrl(export.url)
                val existingByLabel = downloadClientDao.findByLabel(export.label)
                val existingClientId = existingByUrl ?: existingByLabel
                val existingClient = existingClientId?.let { downloadClientDao.getDownloadClientById(it) }

                var uniqueLabel = export.label
                if (existingByUrl != null && existingByLabel != null && existingByLabel != existingByUrl) {
                    uniqueLabel = "${export.label} (Imported)"
                }

                val currentClients = downloadClientDao.getAllDownloadClients()
                val shouldBeSelected = existingClient?.selected
                    ?: currentClients.none { it.selected }

                val client =
                    DownloadClient(
                        id = existingClientId ?: 0L,
                        type = export.type,
                        label = uniqueLabel,
                        url = export.url,
                        username = EncryptedString(export.username),
                        password = EncryptedString(export.password),
                        apiKey = EncryptedString(export.apiKey),
                        noApiKeyRequired = export.noApiKeyRequired,
                        selected = shouldBeSelected,
                        headers = export.headers,
                        localNetworkEnabled = export.localNetworkEnabled,
                        localNetworkSsids = export.localNetworkSsids,
                        localNetworkEndpoint = export.localNetworkEndpoint,
                        showExternalIpAddress = export.showExternalIpAddress,
                    )

                if (client.id != 0L) {
                    downloadClientDao.update(client)
                } else {
                    downloadClientDao.insert(client)
                }
            }
        }

        downloadClientDao.ensureFirstSelectedIfNone()

        backup.customWebpages.forEachIndexed { index, export ->
            if (index in selectedCustomWebpageIndices) {
                val webpage =
                    CustomWebpage(
                        name = export.name,
                        url = export.url,
                        headers = export.headers,
                    )
                customWebpageDao.insert(webpage)
            }
        }

        backup.globalPreferences?.let { global ->
            if (importTabPreferences) {
                global.tabPreferences?.let { preferencesStore.saveTabPreferences(it) }
            }
            if (importUiPreferences) {
                global.useServiceNavLogos?.let { preferencesStore.setUseServiceNavLogos(it) }
                global.hideInstanceSwitcher?.let { preferencesStore.setHideInstanceSwitcher(it) }
                global.useFloatingNavigationBar?.let { preferencesStore.setUseFloatingNavigationBar(it) }
                global.hideFloatingNavigationBarLabels?.let { preferencesStore.setHideFloatingNavigationBarLabels(it) }
                global.overlayTabBackOpensDrawer?.let { preferencesStore.setOverlayTabBackOpensDrawer(it) }
                global.appTheme?.let { preferencesStore.setAppTheme(it) }
                global.appColor?.let { preferencesStore.setAppColor(it) }
                global.searchShowBanners?.let { preferencesStore.setSearchShowBanners(it) }
                global.dualPanelSupport?.let { preferencesStore.setDualPanelSupport(it) }
                global.useColoredActivityCards?.let { preferencesStore.setUseColoredActivityCards(it) }
                global.useColoredCalendarCards?.let { preferencesStore.setUseColoredCalendarCards(it) }
                global.showInfoCards?.forEach { (type, visible) ->
                    preferencesStore.setInfoCardVisibility(type, visible)
                }
                global.calendarFilterState?.let { preferencesStore.saveCalendarFilterState(it) }
                global.downloadQueueSortState?.let { preferencesStore.saveDownloadClientUiState(it) }
                global.dashboardCardsOrder?.let { preferencesStore.updateDashboardCardsOrder(it) }
                global.showDashboardSearch?.let { preferencesStore.setShowDashboardSearch(it) }
                global.queueRemovalPreferences?.let { preferencesStore.saveQueueRemovalPreferences(it) }
                global.downloadDeleteFiles?.let { preferencesStore.setDownloadDeleteFiles(it) }
            }
            if (importIntegrationsPreferences) {
                global.discoverSectionPreferences?.let { preferencesStore.saveDiscoverSectionPreferences(it) }
                global.smartAddSeerrAction?.let { preferencesStore.setSmartAddSeerrAction(it) }
                global.combineSeerrArrMedia?.let { preferencesStore.setCombineSeerrArrMedia(it) }
                global.bazarrDetailsIntegration?.let { preferencesStore.setBazarrDetailsIntegration(it) }
                global.tracearrDetailsIntegration?.let { preferencesStore.setTracearrDetailsIntegration(it) }
                global.unifiedLibrarySearchAllInstances?.let { preferencesStore.setUnifiedLibrarySearchAllInstances(it) }
            }
        }
    }
}
