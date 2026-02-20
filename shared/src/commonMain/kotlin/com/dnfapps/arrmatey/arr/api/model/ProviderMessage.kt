package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ProviderMessage(
    val message: String? = null,
    val type: String? = null
)
