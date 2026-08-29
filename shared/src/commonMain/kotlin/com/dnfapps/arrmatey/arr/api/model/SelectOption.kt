package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SelectOption(
    val value: Int,
    val name: String,
    val order: Int,
    val hint: String,
    val dividerAfter: Boolean,
)
