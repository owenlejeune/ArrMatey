package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TautulliStreamDecisionSerializer::class)
enum class TautulliStreamDecision(val value: String) {
    DirectPlay("direct play"),
    Copy("copy"),
    DirectStream("direct stream"),
    Transcode("transcode"),
    Burn("burn"),
    None("none"),
    Unknown("unknown"),
    ;

    val isDirectPlay: Boolean
        get() = this == DirectPlay

    val isTranscode: Boolean
        get() = this == Transcode

    companion object {
        fun fromString(value: String?): TautulliStreamDecision = when (value?.lowercase()?.trim()) {
            "direct play", "directplay" -> DirectPlay
            "copy" -> Copy
            "direct stream", "directstream" -> DirectStream
            "transcode" -> Transcode
            "burn" -> Burn
            "none" -> None
            else -> Unknown
        }
    }
}

object TautulliStreamDecisionSerializer : KSerializer<TautulliStreamDecision> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TautulliStreamDecision", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TautulliStreamDecision = TautulliStreamDecision.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: TautulliStreamDecision) {
        encoder.encodeString(value.value)
    }
}
