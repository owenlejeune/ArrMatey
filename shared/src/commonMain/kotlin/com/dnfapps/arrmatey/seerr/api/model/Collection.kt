package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Collection(
    val id: Long = 0L,
    val name: String = "",
    val posterPath: String? = null,
    val backdropPath: String? = null,
)
