package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class PreviewPathBody(
    val destinationRoot: String,
    val metadata: AudiobookMetadataBody
)