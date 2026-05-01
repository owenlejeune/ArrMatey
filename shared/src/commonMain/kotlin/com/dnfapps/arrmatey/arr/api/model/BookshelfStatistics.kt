package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.Serializable

@Serializable
data class BookshelfStatistics(
    override val sizeOnDisk: Long,
    override val releaseGroups: List<String> = emptyList(),
    val bookFileCount: Int,
    val bookCount: Int,
    val totalBookCount: Int,
    val percentOfBooks: Float,
    val availableBookCount: Int = 0,
): ArrStatistics