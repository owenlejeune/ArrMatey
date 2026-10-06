package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.datastore.DiscoverSectionPreferences
import com.dnfapps.arrmatey.datastore.InstancePreferenceStoreRepository
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.downloadclient.repository.DownloadClientRepository
import com.dnfapps.arrmatey.downloadclient.usecase.TestDownloadClientConnectionUseCase
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.usecase.TestInstanceConnectionUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateAllPreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstancePreferencesUseCase
import com.dnfapps.arrmatey.model.AppColor
import com.dnfapps.arrmatey.model.AppTheme
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.model.SmartAddSeerrAction
import com.dnfapps.arrmatey.ui.theme.ViewType
import com.dnfapps.arrmatey.utils.Blur
import com.dnfapps.arrmatey.utils.GridDensity
import com.dnfapps.arrmatey.utils.GridSpacing
import com.dnfapps.arrmatey.utils.PosterElevation
import com.dnfapps.arrmatey.utils.PosterRadius
import com.dnfapps.arrmatey.webpage.repository.CustomWebpageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MoreScreenViewModel(
    instanceRepository: InstanceRepository,
    downloadClientRepository: DownloadClientRepository,
    customWebpageRepository: CustomWebpageRepository,
    private val testInstanceConnectionUseCase: TestInstanceConnectionUseCase,
    private val testDownloadClientConnectionUseCase: TestDownloadClientConnectionUseCase,
    private val preferencesStore: PreferencesStore,
    private val instancePreferenceStoreRepository: InstancePreferenceStoreRepository,
    private val updateInstancePreferencesUseCase: UpdateInstancePreferencesUseCase,
    private val updateAllPreferencesUseCase: UpdateAllPreferencesUseCase,
) : ViewModel() {
    val useServiceNavLogos =
        preferencesStore.useServiceNavLogos
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val hideInstanceSwitcher =
        preferencesStore.hideInstanceSwitcher
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val useFloatingNavigationBar =
        preferencesStore.useFloatingNavigationBar
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val hideFloatingNavigationBarLabels =
        preferencesStore.hideFloatingNavigationBarLabels
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val overlayTabBackOpensDrawer =
        preferencesStore.overlayTabBackOpensDrawer
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true,
            )

    val useColoredActivityCards =
        preferencesStore.useColoredActivityCards
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val useColoredCalendarCards =
        preferencesStore.useColoredCalendarCards
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val appTheme =
        preferencesStore.appTheme
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = AppTheme.System,
            )

    val appColor =
        preferencesStore.appColor
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = preferencesStore.defaultAppColor,
            )

    val localNetworkPermissionInfoDismissed =
        preferencesStore.localNetworkPermissionInfoDismissed
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val searchShowBanners =
        preferencesStore.searchShowBanners
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true,
            )

    val dualPanelSupport =
        preferencesStore.dualPanelSupport
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true,
            )

    val smartAddSeerrAction =
        preferencesStore.smartAddSeerrAction
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SmartAddSeerrAction.default,
            )

    val combineSeerrArrMedia =
        preferencesStore.combineSeerrArrMedia
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true,
            )

    val bazarrDetailsIntegration =
        preferencesStore.bazarrDetailsIntegration
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true,
            )

    val tracearrDetailsIntegration =
        preferencesStore.tracearrDetailsIntegration
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true,
            )

    val unifiedLibrarySearchAllInstances =
        preferencesStore.unifiedLibrarySearchAllInstances
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = true,
            )

    val discoverSectionPreferences =
        preferencesStore.discoverSectionPreferences
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DiscoverSectionPreferences(),
            )

    private val _testingStatus = MutableStateFlow<Map<Long, OperationStatus>>(emptyMap())
    val testingStatus: StateFlow<Map<Long, OperationStatus>> = _testingStatus.asStateFlow()

    val instances =
        instanceRepository
            .observeAllInstances()
            .map { instances ->
                instances.sortedBy { it.type }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    val hasSeerr =
        instances
            .map { list -> list.any { it.type == InstanceType.Seerr } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val hasArr =
        instances
            .map { list -> list.any { it.type == InstanceType.Sonarr || it.type == InstanceType.Radarr } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val hasSeerrAndArr =
        instances
            .map { list ->
                val seerr = list.any { it.type == InstanceType.Seerr }
                val arr = list.any { it.type == InstanceType.Sonarr || it.type == InstanceType.Radarr }
                seerr && arr
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val hasBazarr =
        instances
            .map { list -> list.any { it.type == InstanceType.Bazarr } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val hasTracearr =
        instances
            .map { list -> list.any { it.type == InstanceType.Tracearr } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val downloadClients =
        downloadClientRepository
            .observeAllDownloadClients()
            .map { downloadClient ->
                downloadClient.sortedBy { it.type }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    val customWebpages =
        customWebpageRepository
            .getAllWebpages()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    init {
        observeInstances()
    }

    private fun observeInstances() {
        viewModelScope.launch {
            instances.collect { currentInstances ->
                currentInstances.forEach { instance ->
                    val status = _testingStatus.value[instance.id]
                    if (status == null || status is OperationStatus.Error) {
                        testInstance(instance.id)
                    }
                }
            }
        }
        viewModelScope.launch {
            downloadClients.collect { currentClients ->
                currentClients.forEach { client ->
                    val status = _testingStatus.value[client.id + 100_000]
                    if (status == null || status is OperationStatus.Error) {
                        testClient(client.id)
                    }
                }
            }
        }
    }

    private fun testInstance(id: Long) {
        viewModelScope.launch {
            testInstanceConnectionUseCase(id).collect { status ->
                _testingStatus.value =
                    _testingStatus.value.toMutableMap().apply {
                        put(id, status)
                    }
            }
        }
    }

    private fun testClient(id: Long) {
        viewModelScope.launch {
            testDownloadClientConnectionUseCase(id).collect { status ->
                _testingStatus.update {
                    it.toMutableMap().apply {
                        put(id + 100_000, status)
                    }
                }
            }
        }
    }

    fun refreshInstanceConnections() {
        viewModelScope.launch {
            instances.collect { currentInstances ->
                currentInstances.forEach { instance ->
                    testInstance(instance.id)
                }
            }
        }
    }

    fun toggleUseServiceNavLogos() {
        preferencesStore.toggleUseServiceNavLogos()
    }

    fun toggleInstanceSwitcher() {
        preferencesStore.toggleInstanceSwitcher()
    }

    fun toggleUseFloatingNavigationBar() {
        preferencesStore.toggleUseFloatingNavigationBar()
    }

    fun toggleHideFloatingNavigationBarLabels() {
        preferencesStore.toggleHideFloatingNavigationBarLabels()
    }

    fun toggleOverlayTabBackOpensDrawer() {
        preferencesStore.toggleOverlayTabBackOpensDrawer()
    }

    fun toggleUseColoredActivityCards() {
        preferencesStore.toggleUseColoredActivityCards()
    }

    fun toggleUseColoredCalendarCards() {
        preferencesStore.toggleUseColoredCalendarCards()
    }

    fun setAppTheme(theme: AppTheme) {
        preferencesStore.setAppTheme(theme)
    }

    fun setAppColor(color: AppColor) {
        preferencesStore.setAppColor(color)
    }

    fun dismissLocalNetworkPermissionInfo() {
        preferencesStore.dismissLocalNetworkPermissionInfo()
    }

    fun toggleSearchShowBanners() {
        preferencesStore.toggleSearchShowBanners()
    }

    fun toggleDualPanelSupport() {
        preferencesStore.toggleDualPanelSupport()
    }

    fun setSmartAddSeerrAction(action: SmartAddSeerrAction) {
        preferencesStore.setSmartAddSeerrAction(action)
    }

    fun toggleCombineSeerrArrMedia() {
        preferencesStore.toggleCombineSeerrArrMedia()
    }

    fun toggleBazarrDetailsIntegration() {
        preferencesStore.toggleBazarrDetailsIntegration()
    }

    fun toggleTracearrDetailsIntegration() {
        preferencesStore.toggleTracearrDetailsIntegration()
    }

    fun toggleUnifiedLibrarySearchAllInstances() {
        preferencesStore.toggleUnifiedLibrarySearchAllInstances()
    }

    fun updateDiscoverSectionPreferences(prefs: DiscoverSectionPreferences) {
        preferencesStore.saveDiscoverSectionPreferences(prefs)
    }

    fun resetDiscoverSectionPreferences() {
        preferencesStore.resetDiscoverSectionPreferences()
    }

    val arrInstances: StateFlow<List<Instance>> =
        instances
            .map { list -> list.filter { it.type in InstanceType.arrs() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private val _selectedCustomizationInstanceId = MutableStateFlow<Long?>(null)
    val selectedCustomizationInstanceId: StateFlow<Long?> = _selectedCustomizationInstanceId.asStateFlow()

    val selectedCustomizationInstance: StateFlow<Instance?> =
        combine(arrInstances, _selectedCustomizationInstanceId) { list, id ->
            list.find { it.id == id } ?: list.firstOrNull()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val selectedCustomizationPreferences: StateFlow<InstancePreferences> =
        selectedCustomizationInstance
            .flatMapLatest { instance ->
                if (instance == null) {
                    flowOf(InstancePreferences())
                } else {
                    updateAllPreferencesUseCase.syncGlobalPreferencesToInstance(instance.id)
                    instancePreferenceStoreRepository.getInstancePreferences(instance.id).observePreferences()
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = InstancePreferences(),
            )

    fun setSelectedCustomizationInstanceId(id: Long) {
        _selectedCustomizationInstanceId.value = id
    }

    fun updateCustomizationViewType(viewType: ViewType) {
        saveCustomizationPreferences(selectedCustomizationPreferences.value.copy(viewType = viewType))
    }

    fun updateCustomizationApplyGlobally(applyGlobally: Boolean) {
        saveCustomizationPreferences(selectedCustomizationPreferences.value.copy(applyGlobally = applyGlobally))
    }

    fun updateCustomizationShowFullDetails(show: Boolean) {
        saveCustomizationPreferences(selectedCustomizationPreferences.value.copy(showFullDetails = show))
    }

    fun updateCustomizationShowOverlay(show: Boolean) {
        saveCustomizationPreferences(selectedCustomizationPreferences.value.copy(showOverlay = show))
    }

    fun updateCustomizationShowBannerBackground(show: Boolean) {
        saveCustomizationPreferences(selectedCustomizationPreferences.value.copy(showBannerBackground = show))
    }

    fun updateCustomizationIncludeOverview(show: Boolean) {
        saveCustomizationPreferences(selectedCustomizationPreferences.value.copy(includeOverview = show))
    }

    fun updateCustomizationBannerBlur(blur: Blur) {
        saveCustomizationPreferences(selectedCustomizationPreferences.value.copy(bannerBlur = blur))
    }

    fun updateCustomizationGridDensity(density: GridDensity) {
        saveCustomizationPreferences(selectedCustomizationPreferences.value.copy(gridDensity = density))
    }

    fun updateCustomizationGridSpacing(spacing: GridSpacing) {
        saveCustomizationPreferences(selectedCustomizationPreferences.value.copy(gridSpacing = spacing))
    }

    fun updateCustomizationPosterElevation(elevation: PosterElevation) {
        saveCustomizationPreferences(selectedCustomizationPreferences.value.copy(posterElevation = elevation))
    }

    fun updateCustomizationPosterRadius(radius: PosterRadius) {
        saveCustomizationPreferences(selectedCustomizationPreferences.value.copy(posterRadius = radius))
    }

    private fun saveCustomizationPreferences(prefs: InstancePreferences) {
        val instanceId = selectedCustomizationInstance.value?.id ?: return
        viewModelScope.launch {
            if (prefs.applyGlobally) {
                updateAllPreferencesUseCase(prefs)
            } else {
                updateInstancePreferencesUseCase(instanceId, prefs)
            }
        }
    }
}
