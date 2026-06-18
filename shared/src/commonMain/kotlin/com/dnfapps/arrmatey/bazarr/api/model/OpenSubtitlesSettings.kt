package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class OpenSubtitlesSettings(
    val password: String,
    val skip_wrong_fps: Boolean,
    val ssl: Boolean,
    val timeout: Int,
    val use_tag_search: Boolean,
    val username: String,
    val vip: Boolean
)
