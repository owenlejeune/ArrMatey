package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Provider(
    val displayPriority: Int = 0,
    val logoPath: String = "",
    val id: Long = 0L,
    val name: String = "",
)
