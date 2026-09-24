package com.dnfapps.arrmatey.backup.usecase

import com.dnfapps.arrmatey.backup.TransportEncryptor
import com.dnfapps.arrmatey.backup.model.BackupExport
import com.dnfapps.arrmatey.backup.model.CustomWebpageExport
import com.dnfapps.arrmatey.backup.model.DownloadClientExport
import com.dnfapps.arrmatey.backup.model.GlobalPreferencesExport
import com.dnfapps.arrmatey.backup.model.InstanceExport
import com.dnfapps.arrmatey.database.dao.CustomWebpageDao
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.database.DownloadClientDao
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class ExportDataUseCase(
    private val instanceDao: InstanceDao,
    private val downloadClientDao: DownloadClientDao,
    private val customWebpageDao: CustomWebpageDao,
    private val instancePreferenceStoreRepository: InstancePreferenceStoreRepository,
    private val preferencesStore: PreferencesStore,
    private val transportEncryptor: TransportEncryptor,
    private val json: Json,
) {
    suspend operator fun invoke(
        password: String,
        selectedInstanceIds: Set<Long>,
        selectedDownloadClientIds: Set<Long>,
        selectedCustomWebpageIds: Set<Long> = emptySet(),
        includeInstancePreferences: Boolean,
        includeTabPreferences: Boolean,
        includeUiPreferences: Boolean,
        includeIntegrationsPreferences: Boolean = true,
    ): String {
        val instances =
            instanceDao
                .getAllInstances()
                .filter { it.id in selectedInstanceIds }

        val instanceExports =
            instances.map { instance ->
                val preferences =
                    if (includeInstancePreferences) {
                        instancePreferenceStoreRepository
                            .getInstancePreferences(instance.id)
                            .observePreferences()
                            .first()
                    } else {
                        null
                    }

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
                    preferences = preferences,
                )
            }

        val downloadClients =
            downloadClientDao
                .getAllDownloadClients()
                .filter { it.id in selectedDownloadClientIds }

        val downloadClientExports =
            downloadClients.map { client ->
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
                    localNetworkEndpoint = client.localNetworkEndpoint,
                )
            }

        val customWebpages =
            customWebpageDao
                .getAllWebpagesList()
                .filter { it.id in selectedCustomWebpageIds }

        val customWebpageExports =
            customWebpages.map { webpage ->
                CustomWebpageExport(
                    name = webpage.name,
                    url = webpage.url,
                    headers = webpage.headers,
                )
            }

        val globalPreferences =
            if (includeTabPreferences || includeUiPreferences || includeIntegrationsPreferences) {
                GlobalPreferencesExport(
                    tabPreferences = if (includeTabPreferences) preferencesStore.tabPreferences.first() else null,
                    useServiceNavLogos = if (includeUiPreferences) preferencesStore.useServiceNavLogos.first() else null,
                    hideInstanceSwitcher = if (includeUiPreferences) preferencesStore.hideInstanceSwitcher.first() else null,
                    useFloatingNavigationBar = if (includeUiPreferences) preferencesStore.useFloatingNavigationBar.first() else null,
                    appTheme = if (includeUiPreferences) preferencesStore.appTheme.first() else null,
                    appColor = if (includeUiPreferences) preferencesStore.appColor.first() else null,
                    searchShowBanners = if (includeUiPreferences) preferencesStore.searchShowBanners.first() else null,
                    dualPanelSupport = if (includeUiPreferences) preferencesStore.dualPanelSupport.first() else null,
                    useColoredActivityCards = if (includeUiPreferences) preferencesStore.useColoredActivityCards.first() else null,
                    useColoredCalendarCards = if (includeUiPreferences) preferencesStore.useColoredCalendarCards.first() else null,
                    showInfoCards = if (includeUiPreferences) preferencesStore.showInfoCards.first() else null,
                    discoverSectionPreferences =
                        if (includeIntegrationsPreferences) preferencesStore.discoverSectionPreferences.first() else null,
                    smartAddSeerrAction =
                        if (includeIntegrationsPreferences) preferencesStore.smartAddSeerrAction.first() else null,
                    combineSeerrArrMedia =
                        if (includeIntegrationsPreferences) preferencesStore.combineSeerrArrMedia.first() else null,
                    bazarrDetailsIntegration =
                        if (includeIntegrationsPreferences) preferencesStore.bazarrDetailsIntegration.first() else null,
                    tracearrDetailsIntegration =
                        if (includeIntegrationsPreferences) preferencesStore.tracearrDetailsIntegration.first() else null,
                    unifiedLibrarySearchAllInstances =
                        if (includeIntegrationsPreferences) preferencesStore.unifiedLibrarySearchAllInstances.first() else null,
                    calendarFilterState = if (includeUiPreferences) preferencesStore.observeCalendarFilterState().first() else null,
                    downloadQueueSortState = if (includeUiPreferences) preferencesStore.observeDownloadClientUiState().first() else null,
                    dashboardCardsOrder = if (includeUiPreferences) preferencesStore.dashboardCardsOrder.first() else null,
                    showDashboardSearch = if (includeUiPreferences) preferencesStore.showDashboardSearch.first() else null,
                )
            } else {
                null
            }

        val backup =
            BackupExport(
                instances = instanceExports,
                downloadClients = downloadClientExports,
                customWebpages = customWebpageExports,
                globalPreferences = globalPreferences,
            )

        val jsonString = json.encodeToString(backup)
        return transportEncryptor.encrypt(jsonString, password)
    }
}
