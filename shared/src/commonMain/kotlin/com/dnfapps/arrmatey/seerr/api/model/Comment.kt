package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Comment(
    val id: Int,
    val message: String,
    @Contextual val createdAt: Instant? = null,
    @Contextual val updatedAt: Instant? = null,
    val user: RequestUser? = null
)