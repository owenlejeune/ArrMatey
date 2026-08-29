package com.dnfapps.arrmatey.database

import com.dnfapps.arrmatey.database.dao.ConflictField
import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

class InstanceRepository(
    private val instanceDao: InstanceDao,
) {
    fun observeAllInstances(): Flow<List<Instance>> = instanceDao.observeAllInstances()

    fun observeInstancesByType(type: InstanceType): Flow<List<Instance>> = instanceDao.observeInstancesByType(type)

    fun observeSelectedInstance(type: InstanceType): Flow<Instance?> = instanceDao.observeSelectedInstance(type)

    suspend fun getInstanceById(id: Long): Instance? = instanceDao.getInstanceById(id)

    suspend fun getInstancesByType(type: InstanceType): List<Instance> = instanceDao.getInstancesOfType(type)

    val allInstancesFlow: StateFlow<List<Instance>> =
        instanceDao
            .observeAllInstances()
            .stateIn(
                scope = CoroutineScope(Dispatchers.IO),
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    private suspend fun newInstance(instance: Instance): Long {
        val currentInstances = instanceDao.getInstancesOfType(instance.type)
        val shouldBeSelected = currentInstances.none { i -> i.selected }
        val newInstance = instance.copy(selected = shouldBeSelected)
        return instanceDao.insert(newInstance)
    }

    suspend fun createInstance(instance: Instance): InsertResult =
        try {
            val urlConflict = instanceDao.findByUrl(instance.url) != null
            val labelConflict = instanceDao.findByLabel(instance.label) != null

            val conflictFields =
                buildList {
                    if (urlConflict) add(ConflictField.InstanceUrl)
                    if (labelConflict) add(ConflictField.InstanceLabel)
                }

            if (conflictFields.isNotEmpty()) {
                InsertResult.Conflict(fields = conflictFields)
            } else {
                val id = newInstance(instance)
                if (id > 0L) {
                    InsertResult.Success(id)
                } else {
                    InsertResult.Error("Failed to save")
                }
            }
        } catch (e: Exception) {
            InsertResult.Error(e.message ?: "An error occurred")
        }

    suspend fun updateInstance(instance: Instance): InsertResult =
        try {
            val urlConflict = instanceDao.findOtherByUrl(instance.url, instance.id) != null
            val labelConflict = instanceDao.findOtherByLabel(instance.label, instance.id) != null

            val conflictField =
                buildList {
                    if (urlConflict) add(ConflictField.InstanceUrl)
                    if (labelConflict) add(ConflictField.InstanceLabel)
                }

            if (conflictField.isNotEmpty()) {
                InsertResult.Conflict(fields = conflictField)
            } else {
                val rows = instanceDao.update(instance)
                if (rows > 0) {
                    InsertResult.Success(instance.id)
                } else {
                    InsertResult.Error("Failed to update")
                }
            }
        } catch (e: Exception) {
            println("Error during creation: ${e.message}")
            InsertResult.Error(e.message ?: "An error occurred")
        }

    suspend fun deleteInstance(instance: Instance) {
        instanceDao.deleteAndUpdateSelected(instance)
    }

    suspend fun setInstanceActive(instance: Instance) {
        withContext(Dispatchers.IO) {
            instanceDao.setInstanceAsSelected(instance.id, instance.type)
        }
    }
}
