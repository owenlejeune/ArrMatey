package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.usecase.TestInstanceConnectionUseCase
import com.dnfapps.arrmatey.instances.usecase.TestNewInstanceConnectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MoreScreenViewModel(
    instanceRepository: InstanceRepository,
    val testInstanceConnectionUseCase: TestInstanceConnectionUseCase,
    val preferencesStore: PreferencesStore
): ViewModel() {

    val useServiceNavLogos = preferencesStore.useServiceNavLogos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _testingStatus = MutableStateFlow<Map<Long, OperationStatus>>(emptyMap())
    val testingStatus: StateFlow<Map<Long, OperationStatus>> = _testingStatus.asStateFlow()

    val instances = instanceRepository.observeAllInstances()
        .map { instances ->
            instances.sortedBy { it.type }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        observeInstances()
    }

    private fun observeInstances() {
        viewModelScope.launch {
            instances.collect { currentInstances ->
                currentInstances.forEach { instance ->
                    if (!_testingStatus.value.containsKey(instance.id)) {
                        testInstance(instance.id)
                    }
                }
            }
        }
    }

    private fun testInstance(id: Long) {
        viewModelScope.launch {
            testInstanceConnectionUseCase(id).collect { status ->
                _testingStatus.value = _testingStatus.value.toMutableMap().apply {
                    put(id, status)
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

}