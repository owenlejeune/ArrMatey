package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestCount(
    val total: Int = 0,
    val movie: Int = 0,
    val tv: Int = 0,
    val pending: Int = 0,
    val approved: Int = 0,
    val declined: Int = 0,
    val processing: Int = 0,
    val available: Int = 0,
    val completed: Int = 0,
)
