package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AudiobookPreviewPaths(
    val fullPath: String,
    val relativePath: String,
    val root: String
)