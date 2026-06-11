package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.compose.utils.bytesAsFileSizeString
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Instant

@Serializable
sealed class MockMedia(
    override val id: Long = 1,
    override val title: String = "Mock Title",
    override val originalLanguage: Language? = null,
    override val year: Int = 2026,
    override val qualityProfileId: Int = 1,
    override val monitored: Boolean = true,
    override val runtime: Int = 120,
    override val images: List<ArrImage> = emptyList(),
    override val sortTitle: String = title,
    override val overview: String = "Pariatur et eiusmod cillum veniam Lorem anim ea ea consectetur pariatur deserunt commodo ex. Commodo commodo cupidatat quis minim est est nisi aliqua eiusmod reprehenderit sit qui cillum esse. Consectetur voluptate occaecat est Lorem ut ea sit labore incididunt officia incididunt eiusmod pariatur sit.",
    override val path: String? = null,
    override val cleanTitle: String? = null,
    override val titleSlug: String? = null,
    override val rootFolderPath: String? = null,
    override val folder: String? = null,
    override val certification: String? = null,
    override val genres: List<String> = emptyList(),
    override val tags: List<Int> = emptyList(),
    override val alternateTitles: List<AlternateTitle> = emptyList(),
    override val ratings: ArrRatings? = null,
    override val statistics: ArrStatistics? = null,
    override val added: Instant? = null,
    override val status: MediaStatus = MediaStatus.Continuing
): ArrMedia, InstanceTypeIdentifiable {
    @Transient override val guid: Long = 1L
    override fun ratingScore(): Double = 4.5
    @Transient override val statusProgress: Float = 0.5f
    @Transient override val statusColor: Color = Color.Green
    @Transient override val releasedBy: String? = null
    @Transient override val statusString: String = "Continuing"
    override fun setMonitored(monitored: Boolean): ArrMedia = this
    @Transient override val isMissing: Boolean = false

    @Transient val detailString = "Reprehenderit • Et Laboris • ${42069L.bytesAsFileSizeString()}"

    data object Default: MockMedia()
    data object Sonarr: MockMedia(title = "A Totally Awesome Series")
    data object Radarr: MockMedia(title = "A Totally Awesome Movie")
    data object Lidarr: MockMedia(title = "A Totally Awesome Album")
    data object Readarr: MockMedia(title = "A Totally Awesome Book")
    data object Listenarr: MockMedia(title = "A Totally Awesome Book")

}
//data class MockMedia(
//    override val id: Long = 1,
//    override val title: String = "Mock Title",
//    override val originalLanguage: Language? = null,
//    override val year: Int = 2024,
//    override val qualityProfileId: Int = 1,
//    override val monitored: Boolean = true,
//    override val runtime: Int = 120,
//    override val images: List<ArrImage> = emptyList(),
//    override val sortTitle: String = "Mock Title",
//    override val overview: String = "This is a mock overview for preview purposes in the customization sheet.",
//    override val path: String? = null,
//    override val cleanTitle: String? = null,
//    override val titleSlug: String? = null,
//    override val rootFolderPath: String? = null,
//    override val folder: String? = null,
//    override val certification: String? = null,
//    override val genres: List<String> = emptyList(),
//    override val tags: List<Int> = emptyList(),
//    override val alternateTitles: List<AlternateTitle> = emptyList(),
//    override val ratings: ArrRatings? = null,
//    override val statistics: ArrStatistics? = null,
//    override val added: Instant? = null,
//    override val status: MediaStatus = MediaStatus.Continuing,
//) : ArrMedia {
//    @Transient override val guid: Long = 1L
//    override fun ratingScore(): Double = 0.0
//    @Transient override val statusProgress: Float = 0.5f
//    @Transient override val statusColor: Color = Color.Green
//    @Transient override val releasedBy: String? = null
//    @Transient override val statusString: String = "Continuing"
//    override fun setMonitored(monitored: Boolean): ArrMedia = this
//    @Transient override val isMissing: Boolean = false
//}