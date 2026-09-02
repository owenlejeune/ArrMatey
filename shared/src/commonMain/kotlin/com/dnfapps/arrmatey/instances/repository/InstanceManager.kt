package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.database.CredentialMigrationUseCase
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.networking.asSuccess
import dev.shivathapaa.logger.api.Logger
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class InstanceManager(
    private val instanceRepository: InstanceRepository,
    private val httpClientFactory: HttpClientFactory,
    private val credentialMigrationUseCase: CredentialMigrationUseCase,
    private val logger: Logger,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _instanceRepositories =
        MutableStateFlow<Map<Long, InstanceScopedRepository>>(emptyMap())
    val instanceRepositories: StateFlow<Map<Long, InstanceScopedRepository>> = _instanceRepositories

    init {
        scope.launch {
            credentialMigrationUseCase()
        }
        observeInstances()
    }

    private fun observeInstances() {
        scope.launch {
            instanceRepository
                .observeAllInstances()
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
        logger: Logger,
    ): InstanceScopedRepository =
        when (instance.type) {
            InstanceType.Seerr -> SeerrInstanceRepository(instance, httpClient)

            InstanceType.Prowlarr -> ProwlarrInstanceRepository(instance, httpClient)

            InstanceType.Bazarr -> BazarrInstanceRepository(instance, httpClient)

            InstanceType.Sonarr -> SonarrRepository(instance, httpClient, logger)

            InstanceType.Radarr -> RadarrRepository(instance, httpClient, logger)

            InstanceType.Lidarr -> LidarrRepository(instance, httpClient, logger)

            InstanceType.Bookshelf -> ReadarrRepository(instance, httpClient, logger)

            InstanceType.Listenarr -> ListenarrRepository(instance, httpClient, logger)
        }

    fun getArrRepository(instanceId: Long): ArrInstanceRepository? = _instanceRepositories.value[instanceId] as? ArrInstanceRepository?

    fun getSonarrRepository(instanceId: Long): SonarrRepository? = _instanceRepositories.value[instanceId] as? SonarrRepository

    fun getRadarrRepository(instanceId: Long): RadarrRepository? = _instanceRepositories.value[instanceId] as? RadarrRepository

    fun getLidarrRepository(instanceId: Long): LidarrRepository? = _instanceRepositories.value[instanceId] as? LidarrRepository

    fun getReadarrRepository(instanceId: Long): ReadarrRepository? = _instanceRepositories.value[instanceId] as? ReadarrRepository

    fun getListenarrRepository(instanceId: Long): ListenarrRepository? = _instanceRepositories.value[instanceId] as? ListenarrRepository

    fun getSeerrRepository(instanceId: Long): SeerrInstanceRepository? = _instanceRepositories.value[instanceId] as? SeerrInstanceRepository

    fun getProwlarrRepository(instanceId: Long): ProwlarrInstanceRepository? =
        _instanceRepositories.value[instanceId] as? ProwlarrInstanceRepository

    fun getBazarrRepository(instanceId: Long): BazarrInstanceRepository? =
        _instanceRepositories.value[instanceId] as? BazarrInstanceRepository

    fun getRepository(instanceId: Long): InstanceScopedRepository? = _instanceRepositories.value[instanceId]

    fun getSelectedArrRepository(type: InstanceType): Flow<ArrInstanceRepository?> =
        getSelectedArrRepositoryTyped<ArrInstanceRepository>(type)

    fun getSelectedSonarrRepository(): Flow<SonarrRepository?> = getSelectedArrRepositoryTyped<SonarrRepository>(InstanceType.Sonarr)

    fun getSelectedRadarrRepository(): Flow<RadarrRepository?> = getSelectedArrRepositoryTyped<RadarrRepository>(InstanceType.Radarr)

    fun getSelectedLidarrRepository(): Flow<LidarrRepository?> = getSelectedArrRepositoryTyped<LidarrRepository>(InstanceType.Lidarr)

    fun getSelectedReadarrRepository(): Flow<ReadarrRepository?> = getSelectedArrRepositoryTyped<ReadarrRepository>(InstanceType.Bookshelf)

    fun getSelectedListenarrRepository(): Flow<ListenarrRepository?> =
        getSelectedArrRepositoryTyped<ListenarrRepository>(InstanceType.Listenarr)

    private inline fun <reified T : ArrInstanceRepository> getSelectedArrRepositoryTyped(type: InstanceType): Flow<T?> =
        instanceRepository
            .observeSelectedInstance(type)
            .flatMapLatest { instance ->
                if (instance == null) {
                    flowOf(null)
                } else {
                    _instanceRepositories.map { repos -> repos[instance.id] as? T }
                }
            }

    fun getSelectedSeerrRepository(): Flow<SeerrInstanceRepository?> =
        instanceRepository
            .observeSelectedInstance(InstanceType.Seerr)
            .map { instance ->
                instance?.let { getSeerrRepository(it.id) }
            }

    fun getSelectedProwlarrRepository(): Flow<ProwlarrInstanceRepository?> =
        instanceRepository
            .observeSelectedInstance(InstanceType.Prowlarr)
            .flatMapLatest { instance ->
                if (instance == null) {
                    flowOf(null)
                } else {
                    _instanceRepositories.map { repos -> repos[instance.id] as? ProwlarrInstanceRepository }
                }
            }

    fun getSelectedBazarrRepository(): Flow<BazarrInstanceRepository?> =
        instanceRepository
            .observeSelectedInstance(InstanceType.Bazarr)
            .flatMapLatest { instance ->
                if (instance == null) {
                    flowOf(null)
                } else {
                    _instanceRepositories.map { repos -> repos[instance.id] as? BazarrInstanceRepository }
                }
            }

    fun getAllRepositories(): List<InstanceScopedRepository> = _instanceRepositories.value.values.toList()

    fun getAllArrRepositories(): List<ArrInstanceRepository> = _instanceRepositories.value.values.filterIsInstance<ArrInstanceRepository>()

    fun getAllSeerrRepositories(): List<SeerrInstanceRepository> =
        _instanceRepositories.value.values.filterIsInstance<SeerrInstanceRepository>()

    fun getAllBazarrRepositories(): List<BazarrInstanceRepository> =
        _instanceRepositories.value.values.filterIsInstance<BazarrInstanceRepository>()

    fun repositoriesByType(type: InstanceType): Flow<List<InstanceScopedRepository>> =
        instanceRepository
            .observeInstancesByType(type)
            .combine(_instanceRepositories) { instances, repos ->
                instances.mapNotNull { repos[it.id] }
            }

    fun observeAllArrLibraries(): Flow<List<ArrMedia>> {
        return _instanceRepositories.flatMapLatest { repos ->
            val libraries = repos.values.filterIsInstance<ArrInstanceRepository>().map { it.library }
            if (libraries.isEmpty()) return@flatMapLatest flowOf(emptyList())
            combine(libraries) { results ->
                results.flatMap { it?.asSuccess()?.data ?: emptyList() }
            }
        }
    }

    fun getRepositoriesByType(type: InstanceType): List<InstanceScopedRepository> =
        _instanceRepositories.value.values
            .filter { it.instance.type == type }

    fun cleanup() {
        scope.cancel()
    }
}
