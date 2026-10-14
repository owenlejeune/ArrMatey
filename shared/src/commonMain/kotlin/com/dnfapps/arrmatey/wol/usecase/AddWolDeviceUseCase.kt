package com.dnfapps.arrmatey.wol.usecase

import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.wol.model.WolDevice
import com.dnfapps.arrmatey.wol.repository.WolDeviceRepository

class AddWolDeviceUseCase(private val repository: WolDeviceRepository) {
    suspend operator fun invoke(device: WolDevice): InsertResult = repository.addWolDevice(device)
}
