package com.dnfapps.arrmatey.bazarr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class WhisperAiSettings(
    val endpoint: String,
    val loglevel: String,
    val pass_video_name: Boolean,
    val response: Int,
    val timeout: Int
)
