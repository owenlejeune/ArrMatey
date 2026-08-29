package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.state.InstancesState
import com.dnfapps.arrmatey.instances.usecase.ObserveAllInstancesByTypeUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveSelectedInstanceUseCase
import com.dnfapps.arrmatey.instances.usecase.SetInstanceActiveUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InstancesViewModel(
    private val type: InstanceType,
    private val observeAllInstancesByTypeUseCase: ObserveAllInstancesByTypeUseCase,
    private val observeSelectedInstanceUseCase: ObserveSelectedInstanceUseCase,
    private val setInstanceActiveUseCase: SetInstanceActiveUseCase,
) : ViewModel() {
    private val _instancesState = MutableStateFlow(InstancesState(type))
    val instancesState: StateFlow<InstancesState> = _instancesState.asStateFlow()

    init {
        observeInstances()
        observeSelected()
    }

    private fun observeInstances() {
        viewModelScope.launch {
            observeAllInstancesByTypeUseCase(type).collect { list ->
                _instancesState.update { state -> state.copy(instances = list) }
            }
        }
    }

    private fun observeSelected() {
        viewModelScope.launch {
            observeSelectedInstanceUseCase(type).collect { selected ->
                _instancesState.update { state -> state.copy(selectedInstance = selected) }
            }
        }
    }

    fun setInstanceActive(instance: Instance) {
        viewModelScope.launch {
            setInstanceActiveUseCase(instance)
        }
    }
}
