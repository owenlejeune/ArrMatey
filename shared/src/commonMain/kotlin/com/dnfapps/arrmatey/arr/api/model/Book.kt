package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Book(
    val id: Long,
    val authorMetadataId: Int? = null,
    val foreignBookId: String? = null,
    val titleSlug: String? = null,
    val title: String? = null,
    @Contextual val releaseDate: Instant? = null,
    val links: List<ArrLink> = emptyList(),
    val genres: List<String> = emptyList(),
    val relatedBooks: List<Book> = emptyList(),
    val ratings: BookshelfRatings? = null,
    val cleanTitle: String? = null,
    val monitored: Boolean = false,
    val anyEditionOk: Boolean = false,
    @Contextual val added: Instant? = null,
    val addOptions: BookAddOptions? = null,
    val images: List<ArrImage> = emptyList(),

    val instanceId: Long? = null
) {
    fun getCover() = images.firstOrNull {
        it.coverType == CoverType.Cover
    }
}