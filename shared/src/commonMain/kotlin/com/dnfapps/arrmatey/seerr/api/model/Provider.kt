package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Provider(
    val displayPriority: Int,
    val logoPath: String,
    val id: Long,
    val name: String
)