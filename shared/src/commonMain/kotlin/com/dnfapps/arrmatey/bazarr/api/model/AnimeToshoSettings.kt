package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AnimeToshoSettings(
    val anidb_api_client: String,
    val anidb_api_client_ver: Int,
    val search_threshold: Int
)
