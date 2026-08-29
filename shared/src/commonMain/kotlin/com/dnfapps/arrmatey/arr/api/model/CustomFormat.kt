package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomFormat(
    val id: Int,
    val name: String,
    val includeCustomFormatWhenRenaming: Boolean = false,
    val specifications: List<Specification> = emptyList(),
)
