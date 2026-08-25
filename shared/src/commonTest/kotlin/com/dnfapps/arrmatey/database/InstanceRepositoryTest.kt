package com.dnfapps.arrmatey.database

import com.dnfapps.arrmatey.database.dao.InsertResult
import com.dnfapps.arrmatey.database.dao.InstanceDao
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InstanceRepositoryTest {

    private val fakeDao = object : InstanceDao {
        private val instances = MutableStateFlow<List<Instance>>(emptyList())

        override suspend fun insert(instance: Instance): Long {
            val id = (instances.value.size + 1).toLong()
            instances.value += instance.copy(id = id)
            return id
        }

        override suspend fun delete(instance: Instance) {
            instances.value = instances.value.filter { it.id != instance.id }
        }

        override suspend fun update(instance: Instance): Int {
            instances.value = instances.value.map { if (it.id == instance.id) instance else it }
            return 1
        }

        override suspend fun updateAll(instances: List<Instance>) {
            this.instances.value = instances
        }

        override fun observeAllInstances(): Flow<List<Instance>> = instances

        override fun observeInstancesByType(type: InstanceType): Flow<List<Instance>> = MutableStateFlow(instances.value.filter { it.type == type })

        override suspend fun getAllInstances(): List<Instance> = instances.value

        override suspend fun getInstanceById(id: Long): Instance? = instances.value.find { it.id == id }

        override fun observeSelectedInstance(type: InstanceType): Flow<Instance?> = MutableStateFlow(instances.value.find { it.type == type && it.selected })

        override suspend fun getInstancesOfType(type: InstanceType): List<Instance> = instances.value.filter { it.type == type }

        override suspend fun unselectAllOf(type: InstanceType) {
            instances.value = instances.value.map { if (it.type == type) it.copy(selected = false) else it }
        }

        override suspend fun selectInstance(id: Long) {
            instances.value = instances.value.map { if (it.id == id) it.copy(selected = true) else it }
        }

        override suspend fun findByUrl(url: String): Long? = instances.value.find { it.url == url }?.id

        override suspend fun findByLabel(label: String): Long? = instances.value.find { it.label == label }?.id

        override suspend fun findOtherByUrl(url: String, currentId: Long): Long? = instances.value.find { it.url == url && it.id != currentId }?.id

        override suspend fun findOtherByLabel(label: String, currentId: Long): Long? = instances.value.find { it.label == label && it.id != currentId }?.id

        override suspend fun ensureFirstSelectedIfNone(type: InstanceType) {
            if (instances.value.none { it.type == type && it.selected }) {
                val first = instances.value.firstOrNull { it.type == type }
                if (first != null) {
                    selectInstance(first.id)
                }
            }
        }
    }

    private val repository = InstanceRepository(fakeDao)

    @Test
    fun testCreateInstanceSuccess() = runTest {
        val instance = Instance(
            type = InstanceType.Sonarr,
            label = "Test",
            url = "http://test.com",
            apiKey = EncryptedString("key")
        )
        val result = repository.createInstance(instance)
        assertTrue(result is InsertResult.Success)
        assertEquals(1, result.id)
    }

    @Test
    fun testCreateInstanceConflict() = runTest {
        val instance = Instance(
            type = InstanceType.Sonarr,
            label = "Test",
            url = "http://test.com",
            apiKey = EncryptedString("key")
        )
        repository.createInstance(instance)
        
        val result = repository.createInstance(instance)
        assertTrue(result is InsertResult.Conflict)
    }
}
