package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TautulliResponse<T>(
    val result: String,
    val message: String? = null,
    val data: T? = null,
)
