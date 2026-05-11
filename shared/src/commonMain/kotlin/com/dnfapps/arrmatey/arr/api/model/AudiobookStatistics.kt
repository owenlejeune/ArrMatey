package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AudiobookStatistics(
    override val sizeOnDisk: Long,
    override val releaseGroups: List<String> = emptyList()
) : ArrStatistics
