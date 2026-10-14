package com.dnfapps.arrmatey.wol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.wol.model.WolDevice
import com.dnfapps.arrmatey.wol.sendWolPacket
import com.dnfapps.arrmatey.wol.usecase.AddWolDeviceUseCase
import com.dnfapps.arrmatey.wol.usecase.DeleteWolDeviceUseCase
import com.dnfapps.arrmatey.wol.usecase.GetWolDeviceUseCase
import com.dnfapps.arrmatey.wol.usecase.UpdateWolDeviceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WolDeviceConfigurationUiState(
    val id: Long? = null,
    val name: String = "",
    val macAddress: String = "",
    val ipAddress: String = "",
    val port: Int = 9,
    val isEditing: Boolean = false,
    val saveResult: InsertResult? = null,
    val error: String? = null,
) {
    val saveButtonEnabled: Boolean
        get() = name.isNotBlank() && macAddress.isNotBlank()
}

class WolDeviceConfigurationViewModel(
    private val deviceId: Long?,
    private val getWolDeviceUseCase: GetWolDeviceUseCase,
    private val addWolDeviceUseCase: AddWolDeviceUseCase,
    private val updateWolDeviceUseCase: UpdateWolDeviceUseCase,
    private val deleteWolDeviceUseCase: DeleteWolDeviceUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WolDeviceConfigurationUiState())
    val uiState: StateFlow<WolDeviceConfigurationUiState> = _uiState.asStateFlow()

    init {
        if (deviceId != null && deviceId != 0L) {
            viewModelScope.launch {
                getWolDeviceUseCase(deviceId)?.let { device ->
                    _uiState.update {
                        it.copy(
                            id = device.id,
                            name = device.name,
                            macAddress = device.macAddress,
                            ipAddress = device.ipAddress ?: "",
                            port = device.port,
                            isEditing = true,
                        )
                    }
                }
            }
        }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun setMacAddress(macAddress: String) {
        _uiState.update { it.copy(macAddress = macAddress) }
    }

    fun setIpAddress(ipAddress: String) {
        _uiState.update { it.copy(ipAddress = ipAddress) }
    }

    fun setPort(port: Int) {
        _uiState.update { it.copy(port = port) }
    }

    fun saveDevice() {
        val state = _uiState.value
        if (!state.saveButtonEnabled) return
        viewModelScope.launch {
            val device = WolDevice(
                id = state.id ?: 0L,
                name = state.name.trim(),
                macAddress = state.macAddress.trim(),
                ipAddress = state.ipAddress.trim().ifBlank { null },
                port = state.port,
            )
            val result = if (state.isEditing) {
                updateWolDeviceUseCase(device)
            } else {
                addWolDeviceUseCase(device)
            }
            _uiState.update { it.copy(saveResult = result) }
        }
    }

    fun deleteDevice() {
        val id = _uiState.value.id ?: return
        viewModelScope.launch {
            deleteWolDeviceUseCase(id)
            _uiState.update { it.copy(saveResult = InsertResult.Success(id)) }
        }
    }

    fun sendWakeCommand() {
        val state = _uiState.value
        if (state.macAddress.isBlank()) return
        viewModelScope.launch {
            sendWolPacket(state.macAddress, state.ipAddress.ifBlank { null }, state.port)
        }
    }

    val deleteWolDevice = ::deleteDevice

    fun reset() {
        _uiState.update { it.copy(saveResult = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
