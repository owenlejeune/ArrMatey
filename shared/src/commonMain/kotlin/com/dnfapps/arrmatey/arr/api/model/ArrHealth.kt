package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ArrHealth(
    val type: ArrHealthType,
    val wikiUrl: String? = null,
    val source: String? = null,
    val message: String? = null,
)
