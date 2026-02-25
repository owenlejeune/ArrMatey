package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Collection(
    val id: Long,
    val name: String,
    val posterPath: String? = null,
    val backdropPath: String? = null
)