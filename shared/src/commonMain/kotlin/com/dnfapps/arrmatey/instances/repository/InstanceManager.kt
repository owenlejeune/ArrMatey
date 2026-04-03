package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import dev.shivathapaa.logger.api.Logger
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class InstanceManager(
    private val instanceRepository: InstanceRepository,
    private val httpClientFactory: HttpClientFactory,
    private val logger: Logger
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _instanceRepositories =
        MutableStateFlow<Map<Long, InstanceScopedRepository>>(emptyMap())
    val instanceRepositories: StateFlow<Map<Long, InstanceScopedRepository>> = _instanceRepositories

    init {
        observeInstances()
    }

    private fun observeInstances() {
        scope.launch {
            instanceRepository.observeAllInstances()
                .collect { instances ->
                    updateRepositories(instances)
                }
        }
    }

    private fun updateRepositories(instances: List<Instance>) {
        val currentRepos = _instanceRepositories.value.toMutableMap()
        val instanceIds = instances.map { it.id }.toSet()

        currentRepos.keys
            .filterNot { it in instanceIds }
            .forEach { instanceId ->
                currentRepos.remove(instanceId)
            }

        instances.forEach { instance ->
            if (!currentRepos.containsKey(instance.id)) {
                val httpClient = httpClientFactory.create(instance)
                currentRepos[instance.id] = createScopedRepository(instance, httpClient, logger)
            }
        }

        _instanceRepositories.value = currentRepos
    }

    private fun createScopedRepository(
        instance: Instance,
        httpClient: HttpClient,
        logger: Logger
    ): InstanceScopedRepository {
        return when (instance.type) {
            InstanceType.Seerr -> SeerrInstanceRepository(instance, httpClient)

            InstanceType.Prowlarr -> ProwlarrInstanceRepository(instance, httpClient)

            InstanceType.Sonarr,
            InstanceType.Radarr,
            InstanceType.Lidarr,
            InstanceType.Booksehelf -> ArrInstanceRepository(instance, httpClient, logger)
        }
    }

    fun getArrRepository(instanceId: Long): ArrInstanceRepository? =
        _instanceRepositories.value[instanceId] as? ArrInstanceRepository?

    fun getSeerrRepository(instanceId: Long): SeerrInstanceRepository? =
        _instanceRepositories.value[instanceId] as? SeerrInstanceRepository

    fun getProwlarrRepository(instanceId: Long): ProwlarrInstanceRepository? =
        _instanceRepositories.value[instanceId] as? ProwlarrInstanceRepository

    fun getRepository(instanceId: Long): InstanceScopedRepository? =
        _instanceRepositories.value[instanceId]

    fun getSelectedArrRepository(type: InstanceType): Flow<ArrInstanceRepository?> =
        instanceRepository.observeSelectedInstance(type)
            .flatMapLatest { instance ->
                if (instance == null) flowOf(null)
                else _instanceRepositories.map { repos -> repos[instance.id] as? ArrInstanceRepository }
            }

    fun getSelectedSeerrRepository(): Flow<SeerrInstanceRepository?> =
        instanceRepository.observeSelectedInstance(InstanceType.Seerr)
            .map { instance ->
                instance?.let { getSeerrRepository(it.id) }
            }

    fun getSelectedProwlarrRepository(): Flow<ProwlarrInstanceRepository?> =
        instanceRepository.observeSelectedInstance(InstanceType.Prowlarr)
            .flatMapLatest { instance ->
                if (instance == null) flowOf(null)
                else _instanceRepositories.map { repos -> repos[instance.id] as? ProwlarrInstanceRepository }
            }

    fun getAllRepositories(): List<InstanceScopedRepository> {
        return _instanceRepositories.value.values.toList()
    }

    fun getAllArrRepositories(): List<ArrInstanceRepository> {
        return _instanceRepositories.value.values.filterIsInstance<ArrInstanceRepository>()
    }

    fun getAllSeerrRepositories(): List<SeerrInstanceRepository> {
        return _instanceRepositories.value.values.filterIsInstance<SeerrInstanceRepository>()
    }

    fun repositoriesByType(type: InstanceType): Flow<List<InstanceScopedRepository>> =
        instanceRepository.observeInstancesByType(type)
            .combine(_instanceRepositories) { instances, repos ->
                instances.mapNotNull { repos[it.id] }
            }

    fun getRepositoriesByType(type: InstanceType): List<InstanceScopedRepository> {
        return _instanceRepositories.value.values
            .filter { it.instance.type == type }
    }

    fun cleanup() {
        scope.cancel()
    }
}