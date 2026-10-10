package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.arr.api.client.HasArrImages
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrRed
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ComicVolume(
    override val id: Long? = null,
    @SerialName("comicvine_id") val comicvineId: Long? = null,
    override val title: String? = null,
    override val year: Int? = null,
    val publisher: String? = null,
    @Serializable(with = NullableIntOrArraySerializer::class)
    @SerialName("volume_number") val volumeNumber: Int? = null,
    @SerialName("special_version") val specialVersion: String? = null,
    @SerialName("special_version_locked") val specialVersionLocked: Boolean = false,
    @SerialName("description") override val overview: String? = null,
    @SerialName("site_url") val siteUrl: String? = null,
    override val monitored: Boolean = false,
    @SerialName("monitor_new_issues") val monitorNewIssues: Boolean = false,
    override val folder: String? = null,
    @SerialName("root_folder") val rootFolder: Int? = null,
    @SerialName("issue_count") val issueCount: Int = 0,
    @SerialName("issues_downloaded") val issuesDownloaded: Int = 0,
    @SerialName("total_size") val totalSize: Long = 0,
    @SerialName("volume_folder") val volumeFolder: String? = null,
    @SerialName("monitoring_scheme") val monitoringScheme: MonitoringScheme? = MonitoringScheme.All,
    @SerialName("auto_search") val searchOnAdd: Boolean = false,
    val issues: List<ComicIssue> = emptyList(),
    override val images: List<ArrImage> = emptyList(),
    val instanceId: Long? = null,
    val instanceIds: List<Long> = listOfNotNull(instanceId),
) : ArrMedia,
    HasArrImages<ComicVolume>,
    InstanceTypeIdentifiable {

    override val originalLanguage: Language? = null
    override val qualityProfileId: Int = 0
    override val runtime: Int? = null
    override val sortTitle: String? get() = title
    override val path: String? get() = folder
    override val cleanTitle: String? = null
    override val titleSlug: String? = null
    override val rootFolderPath: String? get() = folder
    override val certification: String? = null
    override val genres: List<String> = emptyList()
    override val tags: List<Int> = emptyList()
    override val alternateTitles: List<AlternateTitle> = emptyList()
    override val ratings: ArrRatings? = null
    override val statistics: ArrStatistics? = null

    @Contextual override val added: Instant? = null

    override val status: MediaStatus
        get() = if (issuesDownloaded >= issueCount && issueCount > 0) MediaStatus.Ended else MediaStatus.Continuing

    override val guid: Long
        get() = id ?: comicvineId ?: ((title?.hashCode()?.toLong() ?: 0L) + 300_000)

    override fun ratingScore(): Double = 0.0

    override val statusProgress: Float
        get() = if (issueCount > 0) issuesDownloaded.toFloat() / issueCount.toFloat() else 0f

    override val statusColor: Color
        get() = when {
            issuesDownloaded >= issueCount && issueCount > 0 -> ArrGreen
            issuesDownloaded > 0 -> ArrBlue
            monitored -> ArrRed
            else -> ArrOrange
        }

    override val releasedBy: String?
        get() = publisher

    override val statusString: String
        get() = if (issueCount > 0) "$issuesDownloaded / $issueCount" else status.name

    override val fileSize: Long
        get() = totalSize

    override val isMissing: Boolean
        get() = issuesDownloaded < issueCount

    override fun setMonitored(monitored: Boolean): ArrMedia = copy(monitored = monitored)

    override fun withNewRoot(
        rootFolderPath: String,
        currentRootFolderPath: String?,
    ): ArrMedia = copy(folder = rootFolderPath)

    override fun withLocalImages(instance: Instance): ComicVolume = copy(images = images.map { it.rebuildWithLocalUrls(instance) })

    fun doCopyForCreation(
        monitored: Boolean = this.monitored,
        monitorNewIssues: Boolean = this.monitorNewIssues,
        folder: String? = this.folder,
        volumeFolder: String? = this.volumeFolder,
        specialVersion: String? = this.specialVersion,
        monitoringScheme: MonitoringScheme? = this.monitoringScheme,
        rootFolder: Int? = this.rootFolder,
        searchOnAdd: Boolean = this.searchOnAdd,
    ): ComicVolume = copy(
        monitored = monitored,
        monitorNewIssues = monitorNewIssues,
        folder = folder,
        volumeFolder = volumeFolder,
        specialVersion = specialVersion,
        monitoringScheme = monitoringScheme,
        rootFolder = rootFolder,
        searchOnAdd = searchOnAdd,
    )

    fun doCopyForUpdate(
        monitored: Boolean = this.monitored,
        monitorNewIssues: Boolean = this.monitorNewIssues,
        folder: String? = this.folder,
        volumeFolder: String? = this.volumeFolder,
        specialVersion: String? = this.specialVersion,
        monitoringScheme: MonitoringScheme? = this.monitoringScheme,
        rootFolder: Int? = this.rootFolder,
    ): ComicVolume = copy(
        monitored = monitored,
        monitorNewIssues = monitorNewIssues,
        folder = folder,
        volumeFolder = volumeFolder,
        specialVersion = specialVersion,
        monitoringScheme = monitoringScheme,
        rootFolder = rootFolder,
    )
}
