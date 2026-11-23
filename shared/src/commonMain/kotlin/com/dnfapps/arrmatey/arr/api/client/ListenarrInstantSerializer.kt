package com.dnfapps.arrmatey.arr.api.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

private fun formatInstantString(string: String): String {
    if (!string.contains("T")) {
        return "${string}T00:00:00Z"
    }
    val timePart = string.substringAfter("T")
    if (!timePart.contains("Z") && !timePart.contains("+") && !timePart.contains("-")) {
        return "${string}Z"
    }
    return string
}

object ListenarrInstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("ListenarrInstant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val string = decoder.decodeString()
        val formatted = formatInstantString(string)
        return Instant.parse(formatted)
    }

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) {
        encoder.encodeString(value.toString())
    }
}

object ListenarrNullableInstantSerializer : KSerializer<Instant?> {
    override val descriptor = PrimitiveSerialDescriptor("ListenarrNullableInstant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant? {
        val string = decoder.decodeString()
        if (string.isBlank()) return null

        return try {
            val formatted = formatInstantString(string)
            Instant.parse(formatted)
        } catch (_: Exception) {
            null
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: Instant?,
    ) {
        value?.let { encoder.encodeString(it.toString()) }
    }
}
