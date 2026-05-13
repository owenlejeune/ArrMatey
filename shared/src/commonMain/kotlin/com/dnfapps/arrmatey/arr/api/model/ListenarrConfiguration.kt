package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ListenarrConfiguration(
    val defaultSearchLanguage: String,
    val defaultSearchRegion: String
) {
    constructor(): this("english", "us")
}