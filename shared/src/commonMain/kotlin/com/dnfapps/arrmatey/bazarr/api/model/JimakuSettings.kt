package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class JimakuSettings(
    val api_key: String,
    val enable_ai_subs: Boolean,
    val enable_archives_download: Boolean,
    val enable_name_search_fallback: Boolean
)
