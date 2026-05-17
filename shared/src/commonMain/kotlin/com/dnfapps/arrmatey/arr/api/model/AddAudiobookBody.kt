package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AddAudiobookBody(
    val autoSearch: Boolean,
    val destinationPath: String,
    val monitored: Boolean,
    val metadata: AudiobookMetadataBody
)