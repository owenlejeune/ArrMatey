package com.dnfapps.arrmatey.arr.api.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

object NullableIntOrArraySerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NullableIntOrArray", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int? {
        require(decoder is JsonDecoder)
        return when (val element = decoder.decodeJsonElement()) {
            is JsonNull -> null
            is JsonPrimitive -> element.intOrNull ?: element.content.toIntOrNull()
            is JsonArray -> {
                element.firstOrNull()?.let { first ->
                    if (first is JsonPrimitive) {
                        first.intOrNull ?: first.content.toIntOrNull()
                    } else {
                        null
                    }
                }
            }

            else -> null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeInt(value)
        }
    }
}
