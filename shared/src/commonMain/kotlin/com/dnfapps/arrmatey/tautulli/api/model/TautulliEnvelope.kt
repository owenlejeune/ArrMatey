package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TautulliEnvelope<T>(
    val response: TautulliResponse<T>,
)
