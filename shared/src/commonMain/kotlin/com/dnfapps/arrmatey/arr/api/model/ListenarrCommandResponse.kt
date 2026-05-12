package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ListenarrCommandResponse(
    val success: Boolean,
    val message: String? = null,
    val downloadId: String? = null,
    val indexerUsed: String? = null,
    val downloadClientUsed: String? = null,
    val searchResult: ListenarrSearchResult? = null
)

@Serializable
data class ListenarrSearchResult(
    val id: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val author: String? = null,
    val album: String? = null,
    val category: String? = null,
    val source: String? = null,
    val sourceLink: String? = null,
    val publishedDate: String? = null,
    val format: String? = null,
    val score: Int? = null,
    val size: Long? = null,
    val seeders: Int? = null,
    val leechers: Int? = null,
    val grabs: Int? = null,
    val files: Int? = null,
    val magnetLink: String? = null,
    val torrentUrl: String? = null,
    val nzbUrl: String? = null,
    val downloadType: String? = null,
    val quality: String? = null,
    val indexerId: Int? = null,
    val indexerImplementation: String? = null,
    val resultUrl: String? = null,
    val description: String? = null,
    val publisher: String? = null,
    val subtitle: String? = null,
    val publishYear: String? = null,
    val language: String? = null,
    val runtime: Int? = null,
    val narrator: String? = null,
    val imageUrl: String? = null,
    val asin: String? = null,
    val isbn: List<String>? = null,
    val series: String? = null,
    val seriesNumber: String? = null,
    val productUrl: String? = null,
    val genres: List<String>? = null,
    val isEnriched: Boolean? = null,
    val metadataSource: String? = null,
    val subtitles: String? = null
)
