package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Creator(
    val id: Long = 0L,
    val name: String = "",
    val gender: Int = 0,
    val profilePath: String? = null,
)
