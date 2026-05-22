package com.dnfapps.arrmatey.arr.api.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

object ListenarrInstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("ListenarrInstant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val string = decoder.decodeString()

        val formatted = when {
            !string.contains("T") -> "${string}T00:00:00Z"
            !string.endsWith("Z") && !string.contains("+") -> "${string}Z"
            else -> string
        }

        return Instant.parse(formatted)
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}

object ListenarrNullableInstantSerializer: KSerializer<Instant?> {
    override val descriptor = PrimitiveSerialDescriptor("ListenarrNullableInstant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant? {
        val string = decoder.decodeString()
        if (string.isBlank()) return null

        val formatted = when {
            !string.contains("T") -> "${string}T00:00:00Z"
            !string.endsWith("Z") && !string.contains("+") -> "${string}Z"
            else -> string
        }

        return Instant.parse(formatted)
    }

    override fun serialize(encoder: Encoder, value: Instant?) {
        value?.let { encoder.encodeString(it.toString()) }
    }
}