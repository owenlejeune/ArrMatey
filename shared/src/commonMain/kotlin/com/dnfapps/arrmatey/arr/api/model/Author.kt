package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.arr.api.client.HasArrImages
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrRed
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.time.Instant

@Serializable
data class Author(
    override val id: Long? = null,
    @SerialName("authorName") override val title: String? = null,
    @SerialName("sortName") override val sortTitle: String? = null,
    @SerialName("cleanName") override val cleanTitle: String? = null,
    override val originalLanguage: Language? = null,
    override val year: Int? = null,
    override val runtime: Int? = null,
    override val certification: String? = null,
    override val alternateTitles: List<AlternateTitle> = emptyList(),

    override val qualityProfileId: Int,
    override val monitored: Boolean,
    override val images: List<ArrImage>,
    override val overview: String? = null,
    override val path: String? = null,
    override val titleSlug: String? = null,
    override val rootFolderPath: String? = null,
    override val folder: String? = null,
    override val genres: List<String> = emptyList(),
    override val tags: List<Int> = emptyList(),
    override val ratings: BookshelfRatings,
    override val statistics: BookshelfStatistics? = null,
    @Contextual override val added: Instant? = null,
    override val status: MediaStatus,

    val sortNameLastFirst: String? = null,
    val monitorNewItems: AuthorMonitorType,
    val metadataProfileId: Int,
    val foreignAuthorId: String? = null,
    val links: List<ArrLink> = emptyList(),
    val nextBook: Book? = null,
    val lastBook: Book? = null,
    val addOptions: AuthorAddOptions? = null
): ArrMedia, HasArrImages<Author>, InstanceTypeIdentifiable {
    companion object {
        fun fromJson(value: String): Author {
            return ArrMedia.json.decodeFromString(value)
        }
    }

    override val guid: Long get() = id?: (Random.nextLong() + 200_000)

    override val isMissing: Boolean
        get() = statistics?.let { it.bookFileCount < it.totalBookCount } ?: false

    override fun ratingScore(): Double = ratings.value.toDouble()

    override val statusColor: Color
        get() = when {
            status == MediaStatus.Ended && statistics?.percentOfBooks == 100f -> ArrGreen
            status == MediaStatus.Continuing && statistics?.percentOfBooks == 100f -> ArrBlue
            statistics?.percentOfBooks != 100f && monitored -> ArrRed
            statistics?.percentOfBooks != 100f && !monitored -> ArrOrange
            else -> Color.Unspecified
        }

    override val releasedBy: String? get() = null
    override val statusString: String get() = status.name

    override fun setMonitored(monitored: Boolean): ArrMedia =
        this.copy(monitored = monitored)

    val bookFileCount: Int
        get() = statistics?.bookFileCount ?: 0

    val bookCount: Int
        get() = statistics?.bookCount ?: 0

    val totalBookCount: Int
        get() = statistics?.totalBookCount ?: 0

    override val statusProgress: Float
        get() = statistics?.percentOfBooks?.div(100f) ?: 0f

    override fun withLocalImages(instanceUrl: String): Author =
        copy(images = images.map { it.rebuildWithLocalUrls(instanceUrl) })

    fun copyForCreation(
        monitor: AuthorMonitorType,
        monitorNew: AuthorMonitorType,
        qualityProfileId: Int,
        rootFolderPath: String?,
        tags: List<Int>
    ) = copy(
//        id = 0,
        addOptions = AuthorAddOptions(monitor = monitor),
        monitorNewItems = monitorNew,
        monitored = monitor != AuthorMonitorType.None,
        qualityProfileId = qualityProfileId,
        rootFolderPath = rootFolderPath,
        path = "${rootFolderPath}/${folder}",
        metadataProfileId = 1,
        tags = tags
    )

    fun copyForEdit(
        monitored: Boolean,
        monitorNew: AuthorMonitorType,
        qualityProfileId: Int,
        rootFolderPath: String?,
        tags: List<Int>
    ) = copy(
        monitored = monitored,
        monitorNewItems = monitorNew,
        qualityProfileId = qualityProfileId,
        rootFolderPath = rootFolderPath,
        path = "${rootFolderPath}/${folder}",
        tags = tags
    )
}