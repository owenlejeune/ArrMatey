package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class FormatItem(
    val id: Int? = null,
    val format: Int,
    val name: String? = null,
    val score: Int,
)
