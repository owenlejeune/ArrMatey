package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class AudiobookFile(
    val id: Long,
    val audiobookId: Long? = null,
    val path: String? = null,
    val size: Long? = null,
    val durationSeconds: Double? = null,
    val format: String? = null,
    val container: String? = null,
    val codec: String? = null,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val channels: Int? = null,
    @Serializable(with = ListenarrInstantSerializer::class) val createdAt: Instant? = null,
    val source: String? = null
)
