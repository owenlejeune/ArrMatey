package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AudiobookMetadataBody(
    val abridged: Boolean = false,
    val asin: String,
    val authors: List<String>,
    val description: String,
    val explicit: Boolean = false,
    val genres: List<String>,
    val imageUrl: String,
    val isbn: List<String>,
    val language: String,
    val narrators: List<String>,
    val publishYear: String,
    val publishDate: String,
    val publisher: String,
    val runtime: Int,
    val series: String,
    val seriesMemberships: List<SeriesMembership>,
    val seriesNumber: String,
    val source: String,
    val tags: List<String>,
    val title: String
)