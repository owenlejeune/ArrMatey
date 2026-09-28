package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Genre(
    val id: Long = 0L,
    val name: String = "",
)
