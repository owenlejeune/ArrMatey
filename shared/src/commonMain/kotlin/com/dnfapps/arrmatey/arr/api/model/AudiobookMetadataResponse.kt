package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AudiobookMetadataResponse(
    val source: String,
    val sourceUrl: String,
    val metadata: AudiobookMetadata
)