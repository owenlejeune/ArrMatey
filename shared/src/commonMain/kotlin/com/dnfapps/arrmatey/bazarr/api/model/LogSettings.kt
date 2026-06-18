package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class LogSettings(
    val exclude_filter: String,
    val ignore_case: Boolean,
    val include_filter: String,
    val use_regex: Boolean
)
