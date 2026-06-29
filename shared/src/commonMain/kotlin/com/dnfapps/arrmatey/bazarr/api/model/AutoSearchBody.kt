package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AutoSearchBody(
    val seriesid: Long? = null,
    val radarrid: Long? = null,
    val action: String = "search-missing"
)