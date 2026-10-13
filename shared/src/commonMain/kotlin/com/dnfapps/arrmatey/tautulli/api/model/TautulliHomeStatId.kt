package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TautulliHomeStatIdSerializer::class)
enum class TautulliHomeStatId(val value: String) {
    TopMovies("top_movies"),
    PopularMovies("popular_movies"),
    TopTv("top_tv"),
    PopularTv("popular_tv"),
    TopMusic("top_music"),
    PopularMusic("popular_music"),
    TopUsers("top_users"),
    LastWatched("last_watched"),
    TopPlatforms("top_platforms"),
    Unknown("unknown"),
    ;

    companion object {
        fun fromString(value: String?): TautulliHomeStatId = when (value?.lowercase()?.trim()) {
            "top_movies" -> TopMovies
            "popular_movies" -> PopularMovies
            "top_tv" -> TopTv
            "popular_tv" -> PopularTv
            "top_music" -> TopMusic
            "popular_music" -> PopularMusic
            "top_users" -> TopUsers
            "last_watched" -> LastWatched
            "top_platforms" -> TopPlatforms
            else -> Unknown
        }
    }
}

object TautulliHomeStatIdSerializer : KSerializer<TautulliHomeStatId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TautulliHomeStatId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TautulliHomeStatId = TautulliHomeStatId.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: TautulliHomeStatId) {
        encoder.encodeString(value.value)
    }
}
