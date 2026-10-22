package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class IndexerStatus(
    val indexerId: Long,
    @Contextual val disabledTill: Instant? = null,
    @Contextual val mostRecentFailure: Instant? = null,
    @Contextual val initialFailure: Instant? = null,
    val status: IndexerHealthStatus? = null,
) {
    val hasFailure: Boolean
        get() = status == IndexerHealthStatus.Failing || disabledTill != null || mostRecentFailure != null || initialFailure != null

    val health: IndexerHealthStatus
        get() = if (hasFailure) IndexerHealthStatus.Failing else (status ?: IndexerHealthStatus.Healthy)
}
