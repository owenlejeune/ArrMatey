package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.networking.NetworkResult

sealed interface InstanceScopedRepository {
    val instance: Instance

    suspend fun testConnection(): NetworkResult<Unit>
}
