package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.extensions.formatAsRuntime
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Instant

@Serializable
sealed interface ArrMedia {
    companion object Companion : KoinComponent {
        val json: Json by inject()
        fun fromJson(value: String): ArrMedia {
            return json.decodeFromString(AnyArrMediaSerializer, value)
        }
    }

    /**
     * API JSON properties
     */
    val id: Long?
    val title: String?
    val originalLanguage: Language?
    val year: Int?
    val qualityProfileId: Int
    val monitored: Boolean
    val runtime: Int?
    val images: List<ArrImage>
    val sortTitle: String?
    val overview: String?
    val path: String?
    val cleanTitle: String?
    val titleSlug: String?
    val rootFolderPath: String?
    val folder: String?
    val certification: String?
    val genres: List<String>
    val tags: List<Int>
    val alternateTitles: List<AlternateTitle>
    val ratings: ArrRatings?
    val statistics: ArrStatistics?
    @Contextual val added: Instant?
    val status: MediaStatus

    /**
     * Computed properties + helpers
     */
    val guid: Long
    fun ratingScore(): Double
    val statusProgress: Float
    val statusColor: Color
    val releasedBy: String?
    val statusString: String
    val fileSize: Long
        get() = statistics?.sizeOnDisk ?: 0L
    val runtimeString: String
        get() = runtime?.formatAsRuntime() ?: ""

    fun getPoster(): ArrImage?  {
        return images.firstOrNull { it.coverType == CoverType.Poster }
    }
    fun getBanner(): ArrImage? {
        return images.firstOrNull { it.coverType == CoverType.FanArt }
            ?: images.firstOrNull { it.coverType == CoverType.Banner }
            ?: images.firstOrNull { it.coverType == CoverType.Poster }
    }

    fun getClearLogo(): ArrImage? {
        return images.firstOrNull { it.coverType == CoverType.ClearLogo }
    }
    fun setMonitored(monitored: Boolean): ArrMedia

    fun formatTags(availableTags: List<Tag>): String? = when {
        availableTags.isEmpty() || tags.isEmpty() -> null
        else -> {
            tags.mapNotNull { t -> availableTags.firstOrNull { it.id == t }?.label }
                .joinToString(", ")
                .takeUnless { it.isEmpty() }
        }
    }

    /**
     * Filtering props
     */
    val isMissing: Boolean
    val isWanted: Boolean
        get() = false
    val isDownloaded: Boolean
        get() = false
    val isEnded: Boolean
        get() = status == MediaStatus.Ended
    val isContinuing: Boolean
        get() = status == MediaStatus.Continuing
}

fun ArrMedia.toJson(): String {
    val element: JsonElement = when (this) {
        is ArrSeries -> ArrMedia.json.encodeToJsonElement(ArrSeriesSerializer, this)
        is ArrMovie  -> ArrMedia.json.encodeToJsonElement(ArrMovieSerializer, this)
        is Arrtist -> ArrMedia.json.encodeToJsonElement(ArrtistSerializer, this)
        is Author -> ArrMedia.json.encodeToJsonElement(AuthorSerializer, this)
    }

    return ArrMedia.json.encodeToString(element)
}

object ArrSeriesSerializer :
    JsonTransformingSerializer<ArrSeries>(ArrSeries.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        val obj = element.jsonObject
        return buildJsonObject {
            obj.forEach { (k, v) -> put(k, v) }
            put("mediaType", InstanceType.Sonarr.name)
        }
    }
}

object ArrMovieSerializer :
    JsonTransformingSerializer<ArrMovie>(ArrMovie.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        val obj = element.jsonObject
        return buildJsonObject {
            obj.forEach { (k, v) -> put(k, v) }
            put("mediaType", InstanceType.Radarr.name)
        }
    }
}

object ArrtistSerializer:
    JsonTransformingSerializer<Arrtist>(Arrtist.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        val obj = element.jsonObject
        return buildJsonObject {
            obj.forEach { (k, v) -> put(k, v) }
            put("mediaType", InstanceType.Lidarr.name)
        }
    }
}

object AuthorSerializer:
    JsonTransformingSerializer<Author>(Author.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        val obj = element.jsonObject
        return buildJsonObject {
            obj.forEach { (k, v) -> put(k, v) }
            put("mediaType", InstanceType.Booksehelf.name)
        }
    }
}


object AnyArrMediaSerializer: KSerializer<ArrMedia> {
    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("AnyArrmedia")

    override fun deserialize(decoder: Decoder): ArrMedia {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement()
        val obj = element.jsonObject

        val mediaType = obj["mediaType"]?.jsonPrimitive?.content ?: error("mediaType is missing")

        return when (mediaType) {
            InstanceType.Sonarr.name -> decoder.json.decodeFromJsonElement(ArrSeries.serializer(), element)
            InstanceType.Radarr.name -> decoder.json.decodeFromJsonElement(ArrMovie.serializer(), element)
            InstanceType.Lidarr.name -> decoder.json.decodeFromJsonElement(Arrtist.serializer(), element)
            InstanceType.Booksehelf.name -> decoder.json.decodeFromJsonElement(Author.serializer(), element)
            else -> error("Unknown mediaType: $mediaType")
        }
    }

    override fun serialize(encoder: Encoder, value: ArrMedia) {
        require(encoder is JsonEncoder)
        val json = encoder.json
        val element: JsonElement = when (value) {
            is ArrSeries -> json.encodeToJsonElement(ArrSeriesSerializer, value)
            is ArrMovie  -> json.encodeToJsonElement(ArrMovieSerializer, value)
            is Arrtist -> json.encodeToJsonElement(ArrtistSerializer, value)
            is Author -> json.encodeToJsonElement(AuthorSerializer, value)
        }
        encoder.encodeJsonElement(element)
    }
}