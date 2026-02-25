package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Network(
    val id: Long,
    val logoPath: String? = null,
    val originCountry: String? = null,
    val name: String
)