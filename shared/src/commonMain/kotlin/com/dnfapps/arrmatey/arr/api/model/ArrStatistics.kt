package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.collections.contains

@Serializable(with = ArrStatisticsSerializer::class)
sealed interface ArrStatistics {
    val sizeOnDisk: Long
    val releaseGroups: List<String>
}

object ArrStatisticsSerializer : JsonContentPolymorphicSerializer<ArrStatistics>(ArrStatistics::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ArrStatistics> {
        val jsonObject = element.jsonObject

        return when {
            "episodeFileCount" in jsonObject -> SeriesStatistics.serializer()
            "movieFileCount" in jsonObject -> MovieStatistics.serializer()
            "albumCount" in jsonObject -> LidarrStatistics.serializer()
            "bookCount" in jsonObject -> BookshelfStatistics.serializer()
            "fileCount" in jsonObject -> AudiobookStatistics.serializer()
            else -> throw SerializationException("Unknown MediaItem type")
        }
    }
}