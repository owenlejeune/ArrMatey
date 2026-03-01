package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrRed
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ArrSeries(
    override val id: Long? = null,
    override val title: String? = null,
    override val originalLanguage: Language,
    override val year: Int,
    override val qualityProfileId: Int,
    override var monitored: Boolean,
    override val runtime: Int,
    override val status: MediaStatus,
    override val sortTitle: String? = null,
    override val overview: String? = null,
    override val path: String? = null,
    override val cleanTitle: String? = null,
    override val titleSlug: String? = null,
    override val rootFolderPath: String? = null,
    override val folder: String? = null,
    override val certification: String? = null,
    override val images: List<ArrImage> = emptyList(),
    override val alternateTitles: List<AlternateTitle> = emptyList(),
    override val genres: List<String> = emptyList(),
    override val tags: List<Int> = emptyList(),
    override val ratings: SeriesRatings? = null,
    override val statistics: SeriesStatistics? = null,
    @Contextual override val added: Instant? = null,

    val addOptions: SeriesAddOptions? = null,
    val ended: Boolean,
    val seasonFolder: Boolean,
    val monitorNewItems: MonitorNewItems,
    val useSceneNumbering: Boolean,
    val imdbId: String? = null,
    val tmdbId: Long? = null,
    val tvdbId: Long,
    val tvRageId: Int? = null,
    val tvMazeId: Int? = null,
    val seriesType: SeriesType,
    val seasons: List<Season> = emptyList(),
    val profileName: String? = null,
    @Contextual val nextAiring: Instant? = null,
    @Contextual val previousAiring: Instant? = null,
    val network: String? = null,
    val airTime: String? = null,
    val remotePoster: String? = null,
    val firstAired: String? = null,
    val lastAired: String? = null,
    val episodesChanged: String? = null
): ArrMedia {

    override val guid: Long
        get() = id ?: tvdbId.plus(100_000)

    override fun ratingScore(): Double {
        return ratings?.value ?: 0.toDouble()
    }

    val episodeFileCount: Int
        get() = statistics?.episodeFileCount ?: 0

    val episodeCount: Int
        get() = statistics?.episodeCount ?: 0

    val seasonCount: Int
        get() = seasons.size

    override val statusString: String
        get() = status.name

    override val statusProgress: Float
        get() = (statistics?.percentOfEpisodes?.toFloat() ?: 0f) / 100f

    override val statusColor: Color
        get() = when {
            status == MediaStatus.Ended && statistics?.percentOfEpisodes == 100.toDouble() -> ArrGreen
            status == MediaStatus.Continuing && statistics?.percentOfEpisodes == 100.toDouble() -> ArrBlue
            statistics?.percentOfEpisodes != 100.toDouble() && monitored -> ArrRed
            statistics?.percentOfEpisodes != 100.toDouble() && !monitored -> ArrOrange
            else -> Color.Unspecified
        }

    override val releasedBy: String?
        get() = network

    override fun setMonitored(monitored: Boolean): ArrSeries {
        return copy(monitored = monitored)
    }

    override val isMissing: Boolean
        get() = episodeCount > episodeFileCount

    fun copyForCreation(
        monitor: SeriesMonitorType,
        qualityProfileId: Int,
        seriesType: SeriesType,
        seasonFolder: Boolean,
        rootFolderPath: String?,
        tags: List<Int>
    ) = copy(
        id = 0,
        addOptions = SeriesAddOptions(monitor = monitor),
        qualityProfileId = qualityProfileId,
        seriesType = seriesType,
        seasonFolder = seasonFolder,
        rootFolderPath = rootFolderPath
    )

    fun copyForEdit(
        monitored: Boolean,
        monitorNewSeasons: MonitorNewItems,
        qualityProfileId: Int,
        seriesType: SeriesType,
        seasonFolder: Boolean,
        rootFolderPath: String?,
        tags: List<Int>
    ) = copy(
        monitored = monitored,
        monitorNewItems = monitorNewSeasons,
        qualityProfileId = qualityProfileId,
        seriesType = seriesType,
        seasonFolder = seasonFolder,
        rootFolderPath = rootFolderPath,
        tags = tags
    )

}