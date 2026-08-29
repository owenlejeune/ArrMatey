package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: Int,
    val label: String,
)
