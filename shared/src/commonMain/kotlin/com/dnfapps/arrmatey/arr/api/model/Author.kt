package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.arr.api.client.HasArrImages
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrRed
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Author(
    override val id: Long? = null,
    @SerialName("authorName") override val title: String? = null,
    val authorNameLastFirst: String? = null,
    @SerialName("sortName") override val sortTitle: String? = null,
    @SerialName("cleanName") override val cleanTitle: String? = null,
    override val originalLanguage: Language? = null,
    override val year: Int? = null,
    override val runtime: Int? = null,
    override val certification: String? = null,
    override val alternateTitles: List<AlternateTitle> = emptyList(),
    override val qualityProfileId: Int = 0,
    override val monitored: Boolean = false,
    override val images: List<ArrImage> = emptyList(),
    override val overview: String? = null,
    override val path: String? = null,
    override val titleSlug: String? = null,
    override val rootFolderPath: String? = null,
    override val folder: String? = null,
    override val genres: List<String> = emptyList(),
    override val tags: List<Int> = emptyList(),
    override val ratings: BookshelfRatings = BookshelfRatings(),
    override val statistics: BookshelfStatistics? = null,
    @Contextual override val added: Instant? = null,
    override val status: MediaStatus = MediaStatus.Continuing,
    val sortNameLastFirst: String? = null,
    val monitorNewItems: AuthorMonitorType = AuthorMonitorType.None,
    val metadataProfileId: Int = 0,
    val foreignAuthorId: String? = null,
    val links: List<ArrLink> = emptyList(),
    val nextBook: Book? = null,
    val lastBook: Book? = null,
    val addOptions: AuthorAddOptions? = null,

    // Chaptarr dual profile, path & monitoring fields
    val audiobookQualityProfileId: Int? = null,
    val ebookQualityProfileId: Int? = null,
    val audiobookMetadataProfileId: Int? = null,
    val ebookMetadataProfileId: Int? = null,
    val audiobookRootFolderPath: String? = null,
    val ebookRootFolderPath: String? = null,
    val audiobookFolder: String? = null,
    val ebookFolder: String? = null,
    val audiobookTags: List<Int> = emptyList(),
    val ebookTags: List<Int> = emptyList(),
    val audiobookMonitorExisting: Int? = null,
    val audiobookMonitorFuture: Boolean? = null,
    val ebookMonitorExisting: Int? = null,
    val ebookMonitorFuture: Boolean? = null,
    val lastSelectedMediaType: BookMediaType? = null,
) : ArrMedia,
    HasArrImages<Author>,
    InstanceTypeIdentifiable {
    companion object {
        fun fromJson(value: String): Author = ArrMedia.json.decodeFromString(value)
    }

    override val guid: Long get() = id ?: ((foreignAuthorId?.hashCode()?.toLong() ?: title?.hashCode()?.toLong() ?: 0L) + 200_000)

    override val isMissing: Boolean
        get() = statistics?.let { it.bookFileCount < it.totalBookCount } ?: false

    override fun ratingScore(): Double = ratings.value.toDouble()

    override val statusColor: Color
        get() =
            when {
                status == MediaStatus.Ended && statistics?.percentOfBooks == 100f -> ArrGreen
                status == MediaStatus.Continuing && statistics?.percentOfBooks == 100f -> ArrBlue
                statistics?.percentOfBooks != 100f && monitored -> ArrRed
                statistics?.percentOfBooks != 100f && !monitored -> ArrOrange
                else -> Color.Unspecified
            }

    override val releasedBy: String? get() = null
    override val statusString: String get() = status.name

    override fun setMonitored(monitored: Boolean): ArrMedia = this.copy(monitored = monitored)

    val bookFileCount: Int
        get() = statistics?.bookFileCount ?: 0

    val bookCount: Int
        get() = statistics?.bookCount ?: 0

    val totalBookCount: Int
        get() = statistics?.totalBookCount ?: 0

    override val statusProgress: Float
        get() = statistics?.percentOfBooks?.div(100f) ?: 0f

    val isChaptarr: Boolean
        get() = audiobookQualityProfileId != null ||
            ebookQualityProfileId != null ||
            audiobookRootFolderPath != null ||
            ebookRootFolderPath != null ||
            lastSelectedMediaType != null

    val hasAudiobookConfigured: Boolean
        get() = (audiobookQualityProfileId != null && audiobookQualityProfileId != 0) ||
            !audiobookRootFolderPath.isNullOrEmpty()

    val hasEbookConfigured: Boolean
        get() = (ebookQualityProfileId != null && ebookQualityProfileId != 0) ||
            !ebookRootFolderPath.isNullOrEmpty() ||
            (qualityProfileId != 0 && !rootFolderPath.isNullOrEmpty() && !hasAudiobookConfigured)

    val audiobookMonitorType: AuthorMonitorType
        get() = AuthorMonitorType.fromChaptarrIndex(audiobookMonitorExisting)

    val ebookMonitorType: AuthorMonitorType
        get() = AuthorMonitorType.fromChaptarrIndex(ebookMonitorExisting)

    fun hasMixedMediaTypes(books: List<Book> = emptyList()): Boolean = isChaptarr || books.any { it.mediaType != null } || audiobookQualityProfileId != null

    fun getAudiobookQualityProfile(qualityProfiles: List<QualityProfile>): QualityProfile? = audiobookQualityProfileId?.let { id -> qualityProfiles.firstOrNull { it.id == id } }

    fun getEbookQualityProfile(qualityProfiles: List<QualityProfile>): QualityProfile? = ebookQualityProfileId?.let { id -> qualityProfiles.firstOrNull { it.id == id } }

    fun getAudiobookMetadataProfile(metadataProfiles: List<MetadataProfile>): MetadataProfile? = audiobookMetadataProfileId?.let { id -> metadataProfiles.firstOrNull { it.id == id } }

    fun getEbookMetadataProfile(metadataProfiles: List<MetadataProfile>): MetadataProfile? = ebookMetadataProfileId?.let { id -> metadataProfiles.firstOrNull { it.id == id } }

    fun singleMediaTypeLabel(isChaptarrInstance: Boolean = false): StringResource? {
        val inChaptarr = isChaptarrInstance || isChaptarr
        if (!inChaptarr) return null
        return when {
            hasAudiobookConfigured && !hasEbookConfigured -> MR.strings.audiobook
            hasEbookConfigured && !hasAudiobookConfigured -> MR.strings.ebook
            else -> null
        }
    }

    val singleMediaTypeLabel: StringResource?
        get() = singleMediaTypeLabel(isChaptarrInstance = false)

    override fun withLocalImages(instance: Instance): Author = copy(images = images.map { it.rebuildWithLocalUrls(instance) })

    fun copyForCreation(
        monitor: AuthorMonitorType,
        monitorNew: AuthorMonitorType,
        qualityProfileId: Int,
        rootFolderPath: String?,
        tags: List<Int>,
    ) = copy(
//        id = 0,
        addOptions = AuthorAddOptions(monitor = monitor),
        monitorNewItems = monitorNew,
        monitored = monitor != AuthorMonitorType.None,
        qualityProfileId = qualityProfileId,
        rootFolderPath = rootFolderPath,
        path = "$rootFolderPath/$folder",
        metadataProfileId = 1,
        tags = tags,
    )

    fun copyForChaptarrCreation(
        selectedMediaType: BookMediaType,
        audiobookQualityProfileId: Int?,
        audiobookMetadataProfileId: Int?,
        audiobookRootFolderPath: String?,
        audiobookMonitorExisting: Int?,
        audiobookMonitorFuture: Boolean?,
        audiobookTags: List<Int> = emptyList(),
        ebookQualityProfileId: Int?,
        ebookMetadataProfileId: Int?,
        ebookRootFolderPath: String?,
        ebookMonitorExisting: Int?,
        ebookMonitorFuture: Boolean?,
        ebookTags: List<Int> = emptyList(),
        tags: List<Int> = emptyList(),
        searchForMissingBooks: Boolean = false,
    ): Author {
        val includeAudiobook = selectedMediaType == BookMediaType.Audiobook || selectedMediaType == BookMediaType.Both
        val includeEbook = selectedMediaType == BookMediaType.EBook || selectedMediaType == BookMediaType.Both

        return copy(
            lastSelectedMediaType = selectedMediaType,
            audiobookQualityProfileId = if (includeAudiobook) audiobookQualityProfileId else null,
            audiobookMetadataProfileId = if (includeAudiobook) audiobookMetadataProfileId else null,
            audiobookRootFolderPath = if (includeAudiobook) audiobookRootFolderPath else null,
            audiobookFolder = if (includeAudiobook) folder else null,
            audiobookTags = if (includeAudiobook) audiobookTags else emptyList(),
            audiobookMonitorExisting = if (includeAudiobook) audiobookMonitorExisting else null,
            audiobookMonitorFuture = if (includeAudiobook) audiobookMonitorFuture else null,
            ebookQualityProfileId = if (includeEbook) ebookQualityProfileId else null,
            ebookMetadataProfileId = if (includeEbook) ebookMetadataProfileId else null,
            ebookRootFolderPath = if (includeEbook) ebookRootFolderPath else null,
            ebookFolder = if (includeEbook) folder else null,
            ebookTags = if (includeEbook) ebookTags else emptyList(),
            ebookMonitorExisting = if (includeEbook) ebookMonitorExisting else null,
            ebookMonitorFuture = if (includeEbook) ebookMonitorFuture else null,
            tags = tags,
            qualityProfileId = when (selectedMediaType) {
                BookMediaType.Audiobook -> audiobookQualityProfileId ?: 0
                BookMediaType.EBook -> ebookQualityProfileId ?: 0
                BookMediaType.Both -> audiobookQualityProfileId ?: ebookQualityProfileId ?: 0
            },
            metadataProfileId = when (selectedMediaType) {
                BookMediaType.Audiobook -> audiobookMetadataProfileId ?: 1
                BookMediaType.EBook -> ebookMetadataProfileId ?: 1
                BookMediaType.Both -> audiobookMetadataProfileId ?: ebookMetadataProfileId ?: 1
            },
            rootFolderPath = when (selectedMediaType) {
                BookMediaType.Audiobook -> audiobookRootFolderPath
                BookMediaType.EBook -> ebookRootFolderPath
                BookMediaType.Both -> audiobookRootFolderPath ?: ebookRootFolderPath
            },
            path = when (selectedMediaType) {
                BookMediaType.Audiobook -> audiobookRootFolderPath?.let { "$it/$folder" }
                BookMediaType.EBook -> ebookRootFolderPath?.let { "$it/$folder" }
                BookMediaType.Both -> (audiobookRootFolderPath ?: ebookRootFolderPath)?.let { "$it/$folder" }
            },
            monitored = when (selectedMediaType) {
                BookMediaType.Audiobook -> (audiobookMonitorExisting != null && audiobookMonitorExisting != 0) || audiobookMonitorFuture == true
                BookMediaType.EBook -> (ebookMonitorExisting != null && ebookMonitorExisting != 0) || ebookMonitorFuture == true
                BookMediaType.Both -> (audiobookMonitorExisting != null && audiobookMonitorExisting != 0) || audiobookMonitorFuture == true || (ebookMonitorExisting != null && ebookMonitorExisting != 0) || ebookMonitorFuture == true
            },
            addOptions = AuthorAddOptions(
                monitor = AuthorMonitorType.All,
                searchForMissingBooks = searchForMissingBooks,
            ),
        )
    }

    fun copyForChaptarrEdit(
        audiobookQualityProfileId: Int?,
        audiobookMetadataProfileId: Int?,
        audiobookRootFolderPath: String?,
        audiobookMonitorExisting: Int?,
        audiobookMonitorFuture: Boolean?,
        audiobookTags: List<Int> = emptyList(),
        ebookQualityProfileId: Int?,
        ebookMetadataProfileId: Int?,
        ebookRootFolderPath: String?,
        ebookMonitorExisting: Int?,
        ebookMonitorFuture: Boolean?,
        ebookTags: List<Int> = emptyList(),
        tags: List<Int> = emptyList(),
    ): Author = copy(
        audiobookQualityProfileId = audiobookQualityProfileId,
        audiobookMetadataProfileId = audiobookMetadataProfileId,
        audiobookRootFolderPath = audiobookRootFolderPath,
        audiobookFolder = audiobookFolder ?: folder,
        audiobookTags = audiobookTags,
        audiobookMonitorExisting = audiobookMonitorExisting,
        audiobookMonitorFuture = audiobookMonitorFuture,
        ebookQualityProfileId = ebookQualityProfileId,
        ebookMetadataProfileId = ebookMetadataProfileId,
        ebookRootFolderPath = ebookRootFolderPath,
        ebookFolder = ebookFolder ?: folder,
        ebookTags = ebookTags,
        ebookMonitorExisting = ebookMonitorExisting,
        ebookMonitorFuture = ebookMonitorFuture,
        tags = tags,
        qualityProfileId = audiobookQualityProfileId ?: ebookQualityProfileId ?: qualityProfileId,
        metadataProfileId = audiobookMetadataProfileId ?: ebookMetadataProfileId ?: metadataProfileId,
        rootFolderPath = audiobookRootFolderPath ?: ebookRootFolderPath ?: rootFolderPath,
        path = (audiobookRootFolderPath ?: ebookRootFolderPath ?: rootFolderPath)?.let { "$it/${folder ?: title}" } ?: path,
        monitored = (audiobookMonitorExisting != null && audiobookMonitorExisting != 0) ||
            audiobookMonitorFuture == true ||
            (ebookMonitorExisting != null && ebookMonitorExisting != 0) ||
            ebookMonitorFuture == true,
    )

    fun copyForEdit(
        monitored: Boolean,
        monitorNew: AuthorMonitorType,
        qualityProfileId: Int,
        rootFolderPath: String?,
        tags: List<Int>,
    ) = copy(
        monitored = monitored,
        monitorNewItems = monitorNew,
        qualityProfileId = qualityProfileId,
        rootFolderPath = rootFolderPath,
        path = "$rootFolderPath/$folder",
        tags = tags,
    )

    override fun withNewRoot(
        rootFolderPath: String,
        currentRootFolderPath: String?,
    ): ArrMedia = copy(rootFolderPath = rootFolderPath)
}
