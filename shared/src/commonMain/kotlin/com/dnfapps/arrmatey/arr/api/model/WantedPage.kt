package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class WantedPage(
    val page: Int,
    val pageSize: Int,
    val totalRecords: Int,
    val records: List<ArrMedia>,
)

@Serializable
data class SonarrWantedPage(
    val page: Int,
    val pageSize: Int,
    val totalRecords: Int,
    val records: List<Episode>,
)

@Serializable
data class RadarrWantedPage(
    val page: Int,
    val pageSize: Int,
    val totalRecords: Int,
    val records: List<ArrMovie>,
)
