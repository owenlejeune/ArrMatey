package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.usecase.GetInstancePresencesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetUnifiedMediaDetailsUseCase
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UnifiedMediaDetailsDataObserver(
    private val scope: CoroutineScope,
    private val arrId: Long?,
    private val tmdbId: Long?,
    private val tvdbId: Long?,
    private val resolvedInstanceType: InstanceType?,
    private val resolvedRequestType: RequestType?,
    private val getUnifiedMediaDetailsUseCase: GetUnifiedMediaDetailsUseCase,
    private val getInstancePresencesUseCase: GetInstancePresencesUseCase,
    private val preferencesStore: PreferencesStore,
    private val instanceHandler: UnifiedMediaDetailsInstanceHandler,
    private val arrActionsHandler: UnifiedMediaDetailsArrActionsHandler,
    private val seerrServiceHandler: UnifiedMediaDetailsSeerrServiceHandler,
    private val tracearrHandler: UnifiedMediaDetailsTracearrHandler,
    private val getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
    private val onIsMonitoredUpdated: (Boolean) -> Unit,
) {
    private val quadFlow:
        Flow<Quad<ArrInstanceRepository?, List<ArrInstanceRepository>, SeerrInstanceRepository?, BazarrInstanceRepository?>> =
        combine(
            instanceHandler.activeArrRepoFlow,
            instanceHandler.allArrReposFlow,
            instanceHandler.seerrRepositoryFlow,
            instanceHandler.bazarrRepositoryFlow,
        ) { activeRepo, allRepos, seerrRepo, bazarrRepo ->
            Quad(activeRepo, allRepos, seerrRepo, bazarrRepo)
        }

    fun observeData(uiStateFlow: MutableStateFlow<UnifiedMediaDetailsUiState>) {
        scope.launch {
            instanceHandler.instancePresencesMap.collect { map ->
                val current = uiStateFlow.value as? UnifiedMediaDetailsUiState.Success ?: return@collect
                val updatedPresences =
                    getInstancePresencesUseCase.buildPresencesListFromInstances(
                        instances = current.availableInstances,
                        activeRepoId = current.selectedInstanceId,
                        activeArrMedia = current.arrMedia,
                        presencesMap = map,
                    )
                val effectiveArrMedia =
                    current.arrMedia
                        ?: map[current.selectedInstanceId]
                        ?: map.values.firstOrNull { it != null }
                uiStateFlow.value =
                    current.copy(
                        arrMedia = effectiveArrMedia,
                        instancePresences = updatedPresences,
                    )

                val filteredInstances =
                    current.availableInstances.filter { instance ->
                        val arrMedia = map[instance.id]
                        val isPresent = arrMedia?.let { it.id != null && it.id != 0L } ?: false
                        !isPresent
                    }
                instanceHandler.updateAddSheetUiState { it.copy(availableInstances = filteredInstances) }

                val currentTarget = instanceHandler.addSheetUiState.value.targetInstance
                val activeInst = current.availableInstances.find { it.id == current.selectedInstanceId }
                val newTarget =
                    if (currentTarget != null && filteredInstances.any { it.id == currentTarget.id }) {
                        currentTarget
                    } else if (activeInst != null && filteredInstances.any { it.id == activeInst.id }) {
                        activeInst
                    } else {
                        filteredInstances.firstOrNull()
                    }
                if (newTarget?.id != currentTarget?.id ||
                    instanceHandler.addSheetUiState.value.qualityProfiles
                        .isEmpty()
                ) {
                    instanceHandler.setAddSheetTargetInstance(newTarget)
                }
            }
        }

        seerrServiceHandler.observeSeerrRepo(
            seerrRepoFlow = instanceHandler.seerrRepositoryFlow,
            uiStateFlow = uiStateFlow,
        )

        tracearrHandler.observeTracearrData(
            scope = scope,
            uiStateFlow = uiStateFlow,
            getTracearrInstanceRepositoryUseCase = getTracearrInstanceRepositoryUseCase,
            preferencesStore = preferencesStore,
            initialTmdbId = tmdbId,
            initialRequestType = resolvedRequestType,
        )

        scope.launch {
            quadFlow.collectLatest { (activeRepo, allRepos, seerrRepo, bazarrRepo) ->
                val instances = allRepos.map { it.instance }
                val map = instanceHandler.instancePresencesMap.value
                val filteredInstances =
                    instances.filter { instance ->
                        val arrMedia = map[instance.id]
                        val isPresent = arrMedia?.let { it.id != null && it.id != 0L } ?: false
                        !isPresent
                    }
                instanceHandler.updateAddSheetUiState { it.copy(availableInstances = filteredInstances) }

                val currentTarget = instanceHandler.addSheetUiState.value.targetInstance
                val activeInst = activeRepo?.instance
                val newTarget =
                    if (currentTarget != null && filteredInstances.any { it.id == currentTarget.id }) {
                        currentTarget
                    } else if (activeInst != null && filteredInstances.any { it.id == activeInst.id }) {
                        activeInst
                    } else {
                        filteredInstances.firstOrNull()
                    }
                if (newTarget?.id != currentTarget?.id ||
                    instanceHandler.addSheetUiState.value.qualityProfiles
                        .isEmpty()
                ) {
                    instanceHandler.setAddSheetTargetInstance(newTarget)
                }

                if (activeRepo != null) {
                    if (instanceHandler.selectedInstanceId.value == null) {
                        instanceHandler.setSelectedInstanceId(activeRepo.instance.id)
                        instanceHandler.setInitialInstanceId(activeRepo.instance.id)
                    }
                    launch {
                        activeRepo.qualityProfiles.collect { instanceHandler.updateQualityProfiles(it) }
                    }
                    launch {
                        activeRepo.rootFolders.collect { instanceHandler.updateRootFolders(it) }
                    }
                    launch {
                        activeRepo.tags.collect { instanceHandler.updateTags(it) }
                    }
                    launch {
                        activeRepo.addItemStatus.collect { arrActionsHandler.updateAddItemStatus(it) }
                    }
                    launch {
                        activeRepo.editItemStatus.collect { arrActionsHandler.updateEditStatus(it) }
                    }
                }

                val cachedArrMedia = activeRepo?.let { instanceHandler.instancePresencesMap.value[it.instance.id] }
                val targetArrId =
                    if (activeRepo?.instance?.id == instanceHandler.initialInstanceId) {
                        cachedArrMedia?.id ?: arrId
                    } else {
                        cachedArrMedia?.id
                    }

                val combineMedia = preferencesStore.combineSeerrArrMedia.first()
                val showBazarr = preferencesStore.bazarrDetailsIntegration.first()

                val effectiveSeerrRepo = if (!combineMedia && targetArrId != null) null else seerrRepo
                val effectiveArrRepo = if (!combineMedia && targetArrId == null && seerrRepo != null) null else activeRepo

                getUnifiedMediaDetailsUseCase(
                    arrId = targetArrId,
                    tmdbId = tmdbId,
                    tvdbId = tvdbId,
                    instanceType = resolvedInstanceType,
                    requestType = resolvedRequestType,
                    arrRepository = effectiveArrRepo,
                    seerrRepository = effectiveSeerrRepo,
                    bazarrRepository = if (showBazarr) bazarrRepo else null,
                ).collect { rawState ->
                    if (rawState is UnifiedMediaDetailsUiState.Success) {
                        onIsMonitoredUpdated(rawState.arrMedia?.monitored ?: false)

                        val resolvedTvdbLookupId =
                            tvdbId
                                ?: (rawState.arrMedia as? ArrSeries)?.tvdbId?.takeIf { it > 0 }
                                ?: (rawState.seerrMedia as? TvDetails)?.externalIds?.tvdbId?.takeIf { it > 0 }

                        val resolvedLookupId =
                            tmdbId
                                ?: (rawState.arrMedia as? ArrMovie)?.tmdbId?.takeIf { it > 0 }
                                ?: (rawState.arrMedia as? ArrSeries)?.tmdbId?.takeIf { it > 0 }
                                ?: (rawState.seerrMedia as? MovieDetails)?.id
                                ?: (rawState.seerrMedia as? TvDetails)?.id

                        val query =
                            if (resolvedInstanceType == InstanceType.Sonarr || resolvedRequestType == RequestType.Tv) {
                                resolvedTvdbLookupId?.let { "tvdb:$it" } ?: resolvedLookupId?.let { "tmdb:$it" }
                            } else {
                                resolvedLookupId?.let { "tmdb:$it" } ?: resolvedTvdbLookupId?.let { "tvdb:$it" }
                            }

                        if (activeRepo != null && rawState.hasArrId && rawState.arrMedia != null) {
                            if (instanceHandler.instancePresencesMap.value[activeRepo.instance.id] != rawState.arrMedia) {
                                val updated = instanceHandler.instancePresencesMap.value.toMutableMap()
                                updated[activeRepo.instance.id] = rawState.arrMedia
                                instanceHandler.updatePresencesMap(updated)
                            }
                        }

                        if (allRepos.isNotEmpty() && query != null) {
                            val missingRepos =
                                allRepos.filter { repo ->
                                    instanceHandler.instancePresencesMap.value[repo.instance.id] == null
                                }
                            if (missingRepos.isNotEmpty()) {
                                launch {
                                    val updatedPresences =
                                        getInstancePresencesUseCase.fetchMissingPresences(
                                            repositories = missingRepos,
                                            query = query,
                                            resolvedTvdbLookupId = resolvedTvdbLookupId,
                                            resolvedLookupId = resolvedLookupId,
                                            existingPresences = instanceHandler.instancePresencesMap.value,
                                        )
                                    instanceHandler.updatePresencesMap(updatedPresences)
                                }
                            }
                        }

                        val presences =
                            getInstancePresencesUseCase.buildPresencesList(
                                repositories = allRepos,
                                activeRepoId = activeRepo?.instance?.id,
                                activeArrMedia = rawState.arrMedia,
                                presencesMap = instanceHandler.instancePresencesMap.value,
                            )

                        val effectiveArrMedia =
                            rawState.arrMedia
                                ?: instanceHandler.instancePresencesMap.value[activeRepo?.instance?.id]
                                ?: instanceHandler.instancePresencesMap.value[instanceHandler.selectedInstanceId.value]
                                ?: instanceHandler.instancePresencesMap.value.values.firstOrNull { it != null }

                        uiStateFlow.value =
                            rawState.copy(
                                arrMedia = effectiveArrMedia,
                                availableInstances = if (!combineMedia && !rawState.hasArrId) emptyList() else allRepos.map { it.instance },
                                selectedInstanceId = activeRepo?.instance?.id ?: instanceHandler.selectedInstanceId.value,
                                instancePresences = if (!combineMedia && !rawState.hasArrId) emptyList() else presences,
                                combineSeerrArrMedia = combineMedia,
                                bazarrDetailsIntegration = showBazarr,
                            )
                    } else {
                        if (uiStateFlow.value !is UnifiedMediaDetailsUiState.Success) {
                            uiStateFlow.value = rawState
                        }
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
