package com.dnfapps.arrmatey.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.Flow

@Dao
interface InstanceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(instance: Instance): Long

    @Delete
    suspend fun delete(instance: Instance)

    @Update
    suspend fun update(instance: Instance): Int

    @Update
    suspend fun updateAll(instances: List<Instance>)

    @Query("SELECT * FROM instances")
    fun observeAllInstances(): Flow<List<Instance>>

    @Query("SELECT * FROM instances WHERE type = :type")
    fun observeInstancesByType(type: InstanceType): Flow<List<Instance>>

    @Query("SELECT * FROM instances WHERE type IN (:types)")
    fun observeInstancesByTypes(types: List<InstanceType>): Flow<List<Instance>>

    @Query("SELECT * FROM instances")
    suspend fun getAllInstances(): List<Instance>

    @Query("SELECT * FROM instances WHERE id = :id")
    suspend fun getInstanceById(id: Long): Instance?

    @Query("SELECT * FROM instances WHERE type = :type AND selected = 1 LIMIT 1")
    fun observeSelectedInstance(type: InstanceType): Flow<Instance?>

    @Query("SELECT * FROM instances WHERE type IN (:types) AND selected = 1 LIMIT 1")
    fun observeSelectedInstanceByTypes(types: List<InstanceType>): Flow<Instance?>

    @Query("SELECT * FROM instances WHERE type = :type")
    suspend fun getInstancesOfType(type: InstanceType): List<Instance>

    @Query("SELECT * FROM instances WHERE type IN (:types)")
    suspend fun getInstancesOfTypes(types: List<InstanceType>): List<Instance>

    @Query("UPDATE instances SET selected = 0 WHERE type = :type")
    suspend fun unselectAllOf(type: InstanceType)

    @Query("UPDATE instances SET selected = 0 WHERE type IN (:types)")
    suspend fun unselectAllOfTypes(types: List<InstanceType>)

    @Query("UPDATE instances SET selected = 1 WHERE id = :id")
    suspend fun selectInstance(id: Long)

    @Transaction
    suspend fun setInstanceAsSelected(
        id: Long,
        type: InstanceType,
    ) {
        unselectAllOf(type)
        selectInstance(id)
    }

    @Transaction
    suspend fun setInstanceAsSelected(
        id: Long,
        types: List<InstanceType>,
    ) {
        unselectAllOfTypes(types)
        selectInstance(id)
    }

    @Query("SELECT id FROM instances WHERE url = :url")
    suspend fun findByUrl(url: String): Long?

    @Query("SELECT id FROM instances WHERE label = :label")
    suspend fun findByLabel(label: String): Long?

    @Query("SELECT id FROM instances WHERE url = :url AND id != :currentId LIMIT 1")
    suspend fun findOtherByUrl(
        url: String,
        currentId: Long,
    ): Long?

    @Query("SELECT id FROM instances WHERE label = :label AND id != :currentId LIMIT 1")
    suspend fun findOtherByLabel(
        label: String,
        currentId: Long,
    ): Long?

    @Query(
        """
        UPDATE instances
        SET selected = 1
        WHERE id = (
            SELECT id
            FROM instances AS i
            WHERE i.type = :type
                AND NOT EXISTS (
                    SELECT 1
                    FROM instances AS j
                    WHERE j.type = :type
                        AND j.selected = 1
                )
            ORDER BY i.id
            LIMIT 1
        )
    """,
    )
    suspend fun ensureFirstSelectedIfNone(type: InstanceType)

    @Query(
        """
        UPDATE instances
        SET selected = 1
        WHERE id = (
            SELECT id
            FROM instances AS i
            WHERE i.type IN (:types)
                AND NOT EXISTS (
                    SELECT 1
                    FROM instances AS j
                    WHERE j.type IN (:types)
                        AND j.selected = 1
                )
            ORDER BY i.id
            LIMIT 1
        )
    """,
    )
    suspend fun ensureFirstSelectedIfNone(types: List<InstanceType>)

    @Transaction
    suspend fun deleteAndUpdateSelected(instance: Instance) {
        val type = instance.type
        delete(instance)
        ensureFirstSelectedIfNone(type)
    }
}
