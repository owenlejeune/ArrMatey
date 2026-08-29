package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ArrLink(
    val url: String,
    val name: String,
)
