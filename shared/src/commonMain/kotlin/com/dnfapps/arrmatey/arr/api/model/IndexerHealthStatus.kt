package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class IndexerHealthStatus {
    @SerialName("healthy")
    Healthy,

    @SerialName("failing")
    Failing,
    ;

    val isHealthy: Boolean
        get() = this == Healthy

    val isFailing: Boolean
        get() = this == Failing
}
