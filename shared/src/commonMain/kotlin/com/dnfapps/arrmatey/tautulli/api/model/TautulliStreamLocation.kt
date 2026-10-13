package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TautulliStreamLocationSerializer::class)
enum class TautulliStreamLocation(val value: String) {
    Lan("lan"),
    Wan("wan"),
    Unknown("unknown"),
    ;

    companion object {
        fun fromString(value: String?): TautulliStreamLocation = when (value?.lowercase()?.trim()) {
            "lan" -> Lan
            "wan" -> Wan
            else -> Unknown
        }
    }
}

object TautulliStreamLocationSerializer : KSerializer<TautulliStreamLocation> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TautulliStreamLocation", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TautulliStreamLocation = TautulliStreamLocation.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: TautulliStreamLocation) {
        encoder.encodeString(value.value)
    }
}
