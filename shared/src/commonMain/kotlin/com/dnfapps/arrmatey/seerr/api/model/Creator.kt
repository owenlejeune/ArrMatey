package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Creator(
    val id: Long,
    val name: String,
    val gender: Int,
    val profilePath: String? = null
)