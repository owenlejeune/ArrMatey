package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Video(
    val url: String? = null,
    val key: String = "",
    val name: String = "",
    val size: Int = 0,
    val type: String = "",
    val site: String = "",
)
