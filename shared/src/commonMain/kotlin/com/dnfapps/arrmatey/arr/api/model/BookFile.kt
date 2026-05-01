package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class BookFile(
    val id: Long,
    val authorId: Long? = null,
    val bookId: Long? = null,
    val path: String? = null,
    val size: Long? = null,
    @Contextual val dateAdded: Instant? = null,
    val quality: QualityInfo? = null,
    val qualityWeight: Int? = null,
    val indexerFlags: Int? = null,
    val qualityCutoffNotMet: Boolean? = null
) {
    val fileQualityName: String?
        get() = quality?.qualityLabel
}
