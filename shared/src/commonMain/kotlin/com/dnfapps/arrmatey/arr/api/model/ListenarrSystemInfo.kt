package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ListenarrSystemInfo(
    val version: String,
    val operatingSystem: String,
    val runtime: String,
    val uptime: String,
    val memory: ListenarrDiskSpace,
    @Serializable(with = ListenarrInstantSerializer::class)
    val startTime: Instant,
) {
    fun toArrSoftwareStatus(): ArrSoftwareStatus =
        ArrSoftwareStatus(
            version = version,
            osName = operatingSystem,
            runtimeName = runtime,
        )
}
