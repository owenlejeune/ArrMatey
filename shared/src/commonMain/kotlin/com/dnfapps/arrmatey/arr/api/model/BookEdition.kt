package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookEdition(
    val id: Long,
    val bookId: Long,
    val title: String,
    val foreignEditionId: String,
    val titleSlug: String? = null,
    val isbn13: String? = null,
    val asin: String? = null,
    val language: String,
    val overview: String? = null,
    val format: String,
    val isEbook: Boolean,
    val disambiguation: String? = null,
    val publisher: String? = null,
    val pageCount: Int,
    val releaseDate: String? = null,
    val images: List<BookEditionImage> = emptyList(),
    val links: List<ArrLink> = emptyList(),
    val ratings: BookEditionRatings? = null,
    val monitored: Boolean,
    val manualAdd: Boolean,
    val grabbed: Boolean
)




