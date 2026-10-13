package com.dnfapps.arrmatey.tautulli.api.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

object NullableStringOrIntSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NullableStringOrInt", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        if (decoder !is JsonDecoder) return null
        return when (val element = decoder.decodeJsonElement()) {
            is JsonNull -> null
            is JsonPrimitive -> element.content
            else -> null
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value)
        }
    }
}

@Serializable
data class TautulliHistoryItem(
    val id: Long? = null,
    @SerialName("row_id") val rowId: Long? = null,
    val date: Long? = null,
    val user: String? = null,
    @SerialName("user_id") val userId: Long? = null,
    @SerialName("friendly_name") val friendlyName: String? = null,
    @SerialName("user_thumb") val userThumb: String? = null,
    val platform: String? = null,
    val player: String? = null,
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("media_type") val mediaType: TautulliMediaType? = null,
    @SerialName("rating_key") val ratingKey: String? = null,
    @SerialName("parent_rating_key") val parentRatingKey: String? = null,
    @SerialName("grandparent_rating_key") val grandparentRatingKey: String? = null,
    val title: String? = null,
    @SerialName("parent_title") val parentTitle: String? = null,
    @SerialName("grandparent_title") val grandparentTitle: String? = null,
    @SerialName("full_title") val fullTitle: String? = null,
    val year: Int? = null,
    val duration: Long? = null,
    @SerialName("percent_complete") val percentComplete: Int? = null,
    val stopped: Long? = null,
    @SerialName("paused_counter") val pausedCounter: Long? = null,
    @SerialName("transcode_decision") val transcodeDecision: TautulliStreamDecision? = null,
    val thumb: String? = null,
    @SerialName("parent_thumb") val parentThumb: String? = null,
    @SerialName("grandparent_thumb") val grandparentThumb: String? = null,
    val guid: String? = null,
    @Serializable(with = NullableStringOrIntSerializer::class)
    @SerialName("media_index") val mediaIndex: String? = null,
    @Serializable(with = NullableStringOrIntSerializer::class)
    @SerialName("parent_media_index") val parentMediaIndex: String? = null,
    @Serializable(with = NullableStringOrIntSerializer::class)
    @SerialName("video_resolution") val videoResolution: String? = null,
    @Serializable(with = NullableStringOrIntSerializer::class)
    @SerialName("server_name") val serverName: String? = null,
) {
    val effectiveTitle: String
        get() = when (mediaType) {
            TautulliMediaType.Episode -> {
                val show = grandparentTitle ?: ""
                val ep = title ?: fullTitle ?: ""
                if (show.isNotBlank() && ep.isNotBlank()) "$show - $ep" else ep.ifBlank { show }
            }
            TautulliMediaType.Track -> {
                val artist = grandparentTitle ?: ""
                val track = title ?: ""
                if (artist.isNotBlank() && track.isNotBlank()) "$artist - $track" else track.ifBlank { artist }
            }
            else -> title ?: fullTitle ?: ""
        }

    val effectiveUser: String
        get() = friendlyName?.takeIf { it.isNotBlank() } ?: user?.takeIf { it.isNotBlank() } ?: "Unknown"
}
