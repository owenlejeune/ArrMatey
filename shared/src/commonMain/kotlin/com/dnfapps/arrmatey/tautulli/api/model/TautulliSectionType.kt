package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TautulliSectionTypeSerializer::class)
enum class TautulliSectionType(val value: String) {
    Movie("movie"),
    Show("show"),
    Artist("artist"),
    Photo("photo"),
    Unknown("unknown"),
    ;

    companion object {
        fun fromString(value: String?): TautulliSectionType = when (value?.lowercase()?.trim()) {
            "movie" -> Movie
            "show" -> Show
            "artist" -> Artist
            "photo" -> Photo
            else -> Unknown
        }
    }
}

object TautulliSectionTypeSerializer : KSerializer<TautulliSectionType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TautulliSectionType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TautulliSectionType = TautulliSectionType.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: TautulliSectionType) {
        encoder.encodeString(value.value)
    }
}
