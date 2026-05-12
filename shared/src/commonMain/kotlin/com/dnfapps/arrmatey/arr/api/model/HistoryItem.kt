package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.time.Instant

@Serializable(with = HistoryItemSerializer::class)
sealed interface HistoryItem {
    val id: Long
    val eventType: HistoryEventType
    val date: Instant
    val sourceTitle: String?
    val quality: QualityInfo?
    val languages: List<Language>
    val customFormats: List<CustomFormat>
    val customFormatScore: Int?
    val data: Map<String,String?>

    val displayTitle: String?
        get() = sourceTitle?.split("/")?.last()

    val indexerLabel: String?
        get() = data["indexer"]?.takeUnless { it.isEmpty() }

}

object HistoryItemSerializer : JsonContentPolymorphicSerializer<HistoryItem>(HistoryItem::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<HistoryItem> {
        val jsonObject = element.jsonObject

        return when {
            "movieId" in jsonObject -> RadarrHistoryItem.serializer()
            "seriesId" in jsonObject -> SonarrHistoryItem.serializer()
            "artistId" in jsonObject || "albumId" in jsonObject || "trackId" in jsonObject -> LidarrHistoryItem.serializer()
            "authorId" in jsonObject || "bookId" in jsonObject -> BookshelfHistoryItem.serializer()
            "audiobookId" in jsonObject -> ListenarrHistoryItem.serializer()
            else -> throw SerializationException("Unknown MediaItem type")
        }
    }
}