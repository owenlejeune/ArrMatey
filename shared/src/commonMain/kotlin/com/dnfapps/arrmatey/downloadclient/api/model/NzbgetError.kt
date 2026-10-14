package com.dnfapps.arrmatey.downloadclient.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = NzbgetErrorSerializer::class)
data class NzbgetError(
    val code: Int? = null,
    val message: String? = null,
)

object NzbgetErrorSerializer : KSerializer<NzbgetError> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NzbgetError") {
        element<Int?>("code", isOptional = true)
        element<String?>("message", isOptional = true)
    }

    override fun deserialize(decoder: Decoder): NzbgetError {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            return when {
                element is JsonPrimitive && element.isString -> NzbgetError(message = element.content)
                element is JsonPrimitive -> NzbgetError(message = element.toString())
                element is JsonObject -> {
                    val code = element["code"]?.jsonPrimitive?.intOrNull
                    val msg = element["message"]?.jsonPrimitive?.content ?: element["name"]?.jsonPrimitive?.content
                    NzbgetError(code = code, message = msg)
                }
                else -> NzbgetError(message = element.toString())
            }
        }
        return NzbgetError(message = decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: NzbgetError) {
        val composite = encoder.beginStructure(descriptor)
        value.code?.let { composite.encodeIntElement(descriptor, 0, it) }
        value.message?.let { composite.encodeStringElement(descriptor, 1, it) }
        composite.endStructure(descriptor)
    }
}
