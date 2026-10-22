package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeerrStatus(
    val version: String? = null,
    val commitTag: String? = null,
    @SerialName("updateAvailable") val updateAvailable: Boolean = false,
    @SerialName("commitsBehind") val commitsBehind: Int = 0,
)
