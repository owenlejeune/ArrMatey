package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Revision(
    val version: Int,
    val real: Int,
    val isRepack: Boolean,
)
