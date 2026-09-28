package com.dnfapps.arrmatey.seerr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class DownloadStatus(
    val externalId: Int = 0,
    val mediaType: RequestType = RequestType.Movie,
    val size: Long = 0L,
    val sizeLeft: Long = 0L,
    val status: String = "",
    val title: String = "",
    val downloadId: String = "",
    @Contextual val estimatedCompletionTime: Instant? = null,
)
