package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CustomFilterItem(
    val key: String,
    val value: JsonElement,
    val type: String
)