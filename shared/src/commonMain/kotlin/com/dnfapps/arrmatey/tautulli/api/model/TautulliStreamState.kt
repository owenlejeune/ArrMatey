package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TautulliStreamStateSerializer::class)
enum class TautulliStreamState(val value: String) {
    Playing("playing"),
    Paused("paused"),
    Buffering("buffering"),
    Stopped("stopped"),
    Unknown("unknown"),
    ;

    companion object {
        fun fromString(value: String?): TautulliStreamState = when (value?.lowercase()?.trim()) {
            "playing" -> Playing
            "paused" -> Paused
            "buffering" -> Buffering
            "stopped" -> Stopped
            else -> Unknown
        }
    }
}

object TautulliStreamStateSerializer : KSerializer<TautulliStreamState> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TautulliStreamState", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TautulliStreamState = TautulliStreamState.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: TautulliStreamState) {
        encoder.encodeString(value.value)
    }
}
