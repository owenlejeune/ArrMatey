package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CustomFilter(
    val id: Int,
    val type: String,
    val label: String,
    val filters: List<CustomFilterItem>
)
