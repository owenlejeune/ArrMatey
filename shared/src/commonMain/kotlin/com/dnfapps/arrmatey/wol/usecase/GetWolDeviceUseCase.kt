package com.dnfapps.arrmatey.wol.usecase

import com.dnfapps.arrmatey.wol.model.WolDevice
import com.dnfapps.arrmatey.wol.repository.WolDeviceRepository
import kotlinx.coroutines.flow.Flow

class GetWolDeviceUseCase(private val repository: WolDeviceRepository) {
    suspend operator fun invoke(id: Long): WolDevice? = repository.getWolDeviceById(id)
    fun observe(id: Long): Flow<WolDevice?> = repository.observeWolDeviceById(id)
}
