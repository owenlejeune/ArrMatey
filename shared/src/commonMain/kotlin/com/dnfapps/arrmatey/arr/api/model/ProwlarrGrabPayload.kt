package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ProwlarrGrabPayload(
    val guid: String,
    val indexerId: Int
)
