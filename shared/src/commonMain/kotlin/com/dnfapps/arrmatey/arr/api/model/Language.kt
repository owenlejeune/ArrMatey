package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val id: Int,
    val name: String? = null,
)
