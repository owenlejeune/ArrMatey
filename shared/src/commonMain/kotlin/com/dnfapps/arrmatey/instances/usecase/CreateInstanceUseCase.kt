package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.instances.model.Instance

class CreateInstanceUseCase(
    private val instanceRepository: InstanceRepository,
    private val updateAllPreferencesUseCase: UpdateAllPreferencesUseCase,
) {
    suspend operator fun invoke(instance: Instance): InsertResult {
        val result = instanceRepository.createInstance(instance)
        if (result is InsertResult.Success) {
            updateAllPreferencesUseCase.syncGlobalPreferencesToInstance(result.id)
        }
        return result
    }
}
