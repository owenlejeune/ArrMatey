package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MetadataProfile(
    val id: Int,
    val name: String? = null,
)
