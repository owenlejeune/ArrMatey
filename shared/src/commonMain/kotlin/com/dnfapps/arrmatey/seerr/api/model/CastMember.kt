package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class CastMember(
    val id: Long,
    val castId: Long? = null,
    val character: String,
    val creditId: String,
    val gender: Int,
    val name: String,
    val order: Int,
    val profilePath: String? = null
)