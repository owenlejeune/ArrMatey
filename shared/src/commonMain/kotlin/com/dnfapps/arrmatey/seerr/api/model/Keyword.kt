package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Keyword(
    val id: Long = 0L,
    val name: String = "",
)
