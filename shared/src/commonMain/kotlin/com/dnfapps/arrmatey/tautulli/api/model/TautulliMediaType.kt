package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TautulliMediaTypeSerializer::class)
enum class TautulliMediaType(val value: String) {
    Movie("movie"),
    Show("show"),
    Season("season"),
    Episode("episode"),
    Artist("artist"),
    Album("album"),
    Track("track"),
    Clip("clip"),
    Photo("photo"),
    Live("live"),
    Unknown("unknown"),
    ;

    companion object {
        fun fromString(value: String?): TautulliMediaType = when (value?.lowercase()?.trim()) {
            "movie" -> Movie
            "show" -> Show
            "season" -> Season
            "episode" -> Episode
            "artist" -> Artist
            "album" -> Album
            "track" -> Track
            "clip" -> Clip
            "photo" -> Photo
            "live" -> Live
            else -> Unknown
        }
    }
}

object TautulliMediaTypeSerializer : KSerializer<TautulliMediaType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TautulliMediaType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TautulliMediaType = TautulliMediaType.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: TautulliMediaType) {
        encoder.encodeString(value.value)
    }
}
