package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ReleaseDate(
    val certification: String? = null,
    val iso_639_1: String? = null,
    val note: String? = null,
    @Contextual val release_date: Instant,
    val type: Int
)