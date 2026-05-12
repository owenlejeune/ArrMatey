package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.arr.api.client.ListenarrInstantSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Instant

@Serializable
data class ListenarrHistoryItem(
    override val id: Long,
    override val eventType: HistoryEventType,
    @SerialName("timestamp")
    @Serializable(with = ListenarrInstantSerializer::class)
    override val date: Instant,
    override val sourceTitle: String? = null,
    override val customFormats: List<CustomFormat> = emptyList(),
    override val customFormatScore: Int? = 0,
    @Serializable(with = ListenarrHistoryDataSerializer::class)
    override val data: Map<String, String?> = emptyMap(),

    val audiobookId: Long,
    val audiobookTitle: String,
    val message: String,
    val source: String
): HistoryItem {
    override val languages: List<Language>
        get() = emptyList()

    override val displayTitle: String
        get() = data["File Path"] ?: audiobookTitle

    override val quality: QualityInfo?
        get() = null
}


object ListenarrHistoryDataSerializer: KSerializer<Map<String, String?>> {
    override val descriptor = PrimitiveSerialDescriptor("ListenarrHistoryData", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Map<String, String?> {
        val string = decoder.decodeString()
        val elementMap = Json.decodeFromString<Map<String, JsonElement>>(string)
        return elementMap.mapValues { (_, value) ->
            value.jsonPrimitive.contentOrNull
        }
    }

    override fun serialize(encoder: Encoder, value: Map<String, String?>) {
        encoder.encodeString(Json.encodeToString(value))
    }
}