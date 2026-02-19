package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.HttpClientFactory
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.downloads.repository.DownloadRepository
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class InstanceManager(
    private val instanceRepository: InstanceRepository,
    private val httpClientFactory: HttpClientFactory
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _instanceRepositories =
        MutableStateFlow<Map<Long, InstanceScopedRepository>>(emptyMap())
    val instanceRepositories: StateFlow<Map<Long, InstanceScopedRepository>> = _instanceRepositories

    private val _downloadRepositories =
        MutableStateFlow<Map<Long, DownloadRepository>>(emptyMap())
    val downloadRepositories: StateFlow<Map<Long, DownloadRepository>> = _downloadRepositories

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
        val currentDownloadRepos = _downloadRepositories.value.toMutableMap()
        val instanceIds = instances.map { it.id }.toSet()

        currentRepos.keys
            .filterNot { it in instanceIds }
            .forEach { instanceId ->
                currentRepos.remove(instanceId)
            }
        
        currentDownloadRepos.keys
            .filterNot { it in instanceIds }
            .forEach { instanceId ->
                currentDownloadRepos.remove(instanceId)
            }

        instances.forEach { instance ->
            if (instance.type == InstanceType.QBittorrent || instance.type == InstanceType.Sabnzbd) {
                if (!currentDownloadRepos.containsKey(instance.id)) {
                    val httpClient = httpClientFactory.create(instance)
                    currentDownloadRepos[instance.id] = DownloadRepository(instance, httpClient)
                }
            } else if (!currentRepos.containsKey(instance.id)) {
                val httpClient = httpClientFactory.create(instance)
                currentRepos[instance.id] = InstanceScopedRepository(instance, httpClient)
            }
        }

        _instanceRepositories.value = currentRepos
        _downloadRepositories.value = currentDownloadRepos
    }

    fun getDownloadRepository(instanceId: Long): DownloadRepository? =
        _downloadRepositories.value[instanceId]

    fun getSelectedDownloadRepository(type: InstanceType): Flow<DownloadRepository?> =
        instanceRepository.observeSelectedInstance(type)
            .map { instance ->
                instance?.let { getDownloadRepository(it.id) }
            }

    fun getRepository(instanceId: Long): InstanceScopedRepository? =
        _instanceRepositories.value[instanceId]

    fun getSelectedRepository(type: InstanceType): Flow<InstanceScopedRepository?> =
        instanceRepository.observeSelectedInstance(type)
            .map { instance ->
                instance?.let { getRepository(it.id) }
            }

    fun getAllRepositories(): List<InstanceScopedRepository> {
        return _instanceRepositories.value.values.toList()
    }

    fun repositoriesByType(type: InstanceType): Flow<List<InstanceScopedRepository>> =
        instanceRepository.observeInstancesByType(type)
            .map { instances ->
                val current = _instanceRepositories.value
                instances.mapNotNull { current[it.id] }
            }

    fun getRepositoriesByType(type: InstanceType): List<InstanceScopedRepository> {
        return _instanceRepositories.value.values
            .filter { it.instance.type == type }
    }

    fun refreshAllLibraries() {
        _instanceRepositories.value.values.forEach { repo ->
            scope.launch { repo.refreshLibrary() }
        }
    }

    fun refreshAllMetadata() {
        _instanceRepositories.value.values.forEach { repo ->
            scope.launch { repo.refreshAllMetadata() }
        }
    }

    fun cleanup() {
        scope.cancel()
    }
}