package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Specification(
    val id: Int,
    val name: String,
    val implementation: String,
    val implementationName: String,
    val infoLink: String,
    val negate: Boolean,
    val required: Boolean,
    val fields: List<Field>,
    val presets: List<String>,
)
