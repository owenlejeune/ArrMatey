package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class DownloadStatus(
    val externalId: Int,
    val mediaType: RequestType,
    val size: Long,
    val sizeLeft: Long,
    val status: String,
    val title: String,
    val downloadId: String,
    @Contextual val estimatedCompletionTime: Instant? = null
)