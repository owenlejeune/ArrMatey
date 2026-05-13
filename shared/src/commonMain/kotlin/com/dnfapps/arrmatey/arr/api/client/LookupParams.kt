package com.dnfapps.arrmatey.arr.api.client

import kotlinx.serialization.Serializable

@Serializable
data class LookupParams(
    val query: String,
    val language: String? = null,
    val region: String? = null
) {
    constructor(): this("")
}