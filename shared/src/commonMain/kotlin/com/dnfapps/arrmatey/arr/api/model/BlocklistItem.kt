package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.time.Instant

@Serializable(with = BlocklistItemSerializer::class)
sealed interface BlocklistItem {
    val id: Long
    val sourceTitle: String?
    val quality: QualityInfo?
    val date: Instant
    val protocol: ReleaseProtocol?
    val indexer: String?
    val message: String?
    val languages: List<Language>
    val customFormats: List<CustomFormat>
    val customFormatScore: Int?
    val data: Map<String, String?>
    val instanceId: Long?
    val instanceName: String?
    val instanceType: InstanceType?

    val displayTitle: String?
        get() = sourceTitle?.split("/")?.last()

    val indexerLabel: String?
        get() = data["indexer"]?.takeUnless { it.isEmpty() } ?: indexer
}

object BlocklistItemSerializer : JsonContentPolymorphicSerializer<BlocklistItem>(BlocklistItem::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<BlocklistItem> {
        val jsonObject = element.jsonObject

        return when {
            "movieId" in jsonObject -> RadarrBlocklistItem.serializer()
            "seriesId" in jsonObject -> SonarrBlocklistItem.serializer()
            "artistId" in jsonObject || "albumId" in jsonObject || "trackId" in jsonObject -> LidarrBlocklistItem.serializer()
            "authorId" in jsonObject || "bookId" in jsonObject -> BookshelfBlocklistItem.serializer()
            else -> SonarrBlocklistItem.serializer()
        }
    }
}
