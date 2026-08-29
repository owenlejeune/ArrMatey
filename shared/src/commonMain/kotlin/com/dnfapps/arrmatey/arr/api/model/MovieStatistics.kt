package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieStatistics(
    val movieFileCount: Int,
    override val sizeOnDisk: Long,
    override val releaseGroups: List<String>,
) : ArrStatistics
