package com.dnfapps.arrmatey.arr.api.model

import androidx.compose.ui.graphics.Color
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.theme.ArrGreen
import com.dnfapps.arrmatey.ui.theme.ArrGrey
import com.dnfapps.arrmatey.ui.theme.ArrRed
import com.dnfapps.arrmatey.ui.theme.ArrOrange
import com.dnfapps.arrmatey.ui.theme.ArrBlue
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class ArrMovie(
    override val id: Long? = null,
    override val title: String? = null,
    override val originalLanguage: Language,
    override val year: Int,
    override val qualityProfileId: Int,
    override val monitored: Boolean,
    override val runtime: Int,
    override val status: MediaStatus,
    override val sortTitle: String? = null,
    override val overview: String? = null,
    override val path: String? = null,
    override val cleanTitle: String? = null,
    override val titleSlug: String? = null,
    override val rootFolderPath: String = "",
    override val folder: String? = null,
    override val certification: String? = null,
    override val images: List<ArrImage> = emptyList(),
    override val alternateTitles: List<AlternateTitle> = emptyList(),
    override val genres: List<String> = emptyList(),
    override val tags: List<Int> = emptyList(),
    override val ratings: MovieRatings? = null,
    override val statistics: MovieStatistics? = null,
    @Contextual override val added: Instant? = null,

    val imdbId: String? = null,
    val tmdbId: Long,
    val originalTitle: String? = null,
    val secondaryYear: Int? = null,
    val secondaryYearSourceId: Int,
    val sizeOnDisk: Long = 0,
    @Contextual val inCinemas: Instant? = null,
    @Contextual val physicalRelease: Instant? = null,
    @Contextual val digitalRelease: Instant? = null,
    @Contextual val releaseDate: Instant? = null,
    val physicalReleaseNote: String? = null,
    val website: String? = null,
    val remotePoster: String? = null,
    val youTubeTrailerId: String? = null,
    val studio: String? = null,
    val hasFile: Boolean = false,
    val movieFileId: Int? = null,
    val minimumAvailability: MediaStatus,
    val isAvailable: Boolean = false,
    val folderName: String? = null,
    val keywords: List<String> = emptyList(),
    val movieFile: MovieFile? = null,
    val collection: MovieCollection? = null,
    val popularity: Double = 0.toDouble(),
    val lastSearchTime: String? = null,

    val instanceId: Long? = null
): ArrMedia {

    override val guid: Long
        get() = id ?: tmdbId.plus(100_000)

    val isWaiting: Boolean
        get() = when(status) {
            MediaStatus.Tba, MediaStatus.Announced -> true
            MediaStatus.InCinemas -> minimumAvailability == MediaStatus.Released
            else -> false
        }

    val grabbed: Instant?
        get() = movieFile?.dateAdded

    override fun ratingScore(): Double {
        val imdb = ratings?.imdb?.value
        val rt = ratings?.rottenTomatoes?.value?.apply { this/10 }
        val tmdb = ratings?.tmdb?.value
        val mtc = ratings?.metacritic?.value?.apply { this/10 }
        val trakt = ratings?.trakt?.value

        val avail = listOfNotNull(imdb, rt, tmdb, mtc, trakt)
        return avail.sum() / avail.size
    }

    override val statusString: String
        get() = status.name

    override val statusProgress: Float
        get() = if(movieFile == null) 0f else 1f

    override val statusColor: Color
        get() = when {
            status == MediaStatus.Tba || status == MediaStatus.Announced -> ArrBlue
            movieFile != null && monitored -> ArrGreen
            movieFile != null && !monitored -> ArrGrey
            movieFile == null && monitored -> ArrRed
            movieFile == null && !monitored -> ArrOrange
            else -> Color.Unspecified
        }

    override val releasedBy: String?
        get() = studio


    override fun setMonitored(monitored: Boolean): ArrMovie {
        return copy(monitored = monitored)
    }

    override val isMissing: Boolean
        get() = movieFile == null

    override val isDownloaded: Boolean
        get() = movieFile != null

    override val isWanted: Boolean
        get() = monitored && movieFile == null

    val closestFutureRelease: Pair<StringResource, Instant>?
        get() {
            val now = Clock.System.now()
            return listOfNotNull(
                inCinemas?.let { MR.strings.in_cinemas to it },
                digitalRelease?.let { MR.strings.digital_release to it },
                physicalRelease?.let { MR.strings.physical_release to it }
            )
                .filter { it.second > now }
                .minByOrNull { it.second }
        }

    fun copyForCreation(
        monitored: Boolean,
        minimumAvailability: MediaStatus,
        qualityProfileId: Int,
        rootFolderPath: String,
        tags: List<Int>
    ) = copy(
        id = 0,
        alternateTitles = alternateTitles.filter { it.title != null },
        monitored = monitored,
        minimumAvailability = minimumAvailability,
        qualityProfileId = qualityProfileId,
        rootFolderPath = rootFolderPath,
        tags = tags
    )

    fun copyForUpdate(
        monitored: Boolean,
        minimumAvailability: MediaStatus,
        qualityProfileId: Int,
        rootFolderPath: String,
        tags: List<Int>
    ) = copy(
        monitored = monitored,
        minimumAvailability = minimumAvailability,
        qualityProfileId = qualityProfileId,
        rootFolderPath = rootFolderPath,
        tags = tags
    )

}