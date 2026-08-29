package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Quality(
    val id: Int,
    val name: String,
    val source: String? = null,
    val resolution: Int? = null,
    val modifier: String? = null,
)
