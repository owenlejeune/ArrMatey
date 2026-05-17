package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AddAudiobookResponse(
    val message: String,
    val audiobook: Audiobook
)