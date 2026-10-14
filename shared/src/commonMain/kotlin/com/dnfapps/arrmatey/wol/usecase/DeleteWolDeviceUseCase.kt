package com.dnfapps.arrmatey.wol.usecase

import com.dnfapps.arrmatey.wol.model.WolDevice
import com.dnfapps.arrmatey.wol.repository.WolDeviceRepository

class DeleteWolDeviceUseCase(private val repository: WolDeviceRepository) {
    suspend operator fun invoke(device: WolDevice) = repository.deleteWolDevice(device)
    suspend operator fun invoke(id: Long) = repository.deleteWolDeviceById(id)
}
