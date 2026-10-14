package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray

@Serializable(with = NzbgetEditQueueParamsSerializer::class)
data class NzbgetEditQueueParams(
    val action: NzbgetEditQueueAction,
    val offset: Int = 0,
    val text: String = "",
    val ids: List<Int>,
)

object NzbgetEditQueueParamsSerializer : KSerializer<NzbgetEditQueueParams> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NzbgetEditQueueParams")

    override fun serialize(encoder: Encoder, value: NzbgetEditQueueParams) {
        val jsonEncoder = encoder as? JsonEncoder
        if (jsonEncoder != null) {
            val array = buildJsonArray {
                add(JsonPrimitive(value.action.value))
                add(JsonPrimitive(value.offset))
                add(JsonPrimitive(value.text))
                add(
                    buildJsonArray {
                        value.ids.forEach { add(JsonPrimitive(it)) }
                    },
                )
            }
            jsonEncoder.encodeJsonElement(array)
        }
    }

    override fun deserialize(decoder: Decoder): NzbgetEditQueueParams = throw UnsupportedOperationException("Deserialization of NzbgetEditQueueParams is not supported")
}
