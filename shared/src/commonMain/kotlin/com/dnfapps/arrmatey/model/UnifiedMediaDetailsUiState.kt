package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.api.model.BookSeries
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.arr.api.model.LidarrTrack
import com.dnfapps.arrmatey.arr.api.model.LidarrTrackFile
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.RatingItem
import com.dnfapps.arrmatey.arr.api.model.toRatingItems
import com.dnfapps.arrmatey.bazarr.state.BazarrDetails
import com.dnfapps.arrmatey.extensions.formatMinutesAsRuntime
import com.dnfapps.arrmatey.extensions.getUpcomingDateString
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.seerr.api.model.ImdbRating
import com.dnfapps.arrmatey.seerr.api.model.Keyword
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.RottenTomatoesRating
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.format
import kotlin.math.roundToInt

sealed interface UnifiedMediaDetailsUiState {
    object Initial : UnifiedMediaDetailsUiState

    object Loading : UnifiedMediaDetailsUiState

    data class Error(
        val message: String?,
    ) : UnifiedMediaDetailsUiState

    data class Success(
        val arrMedia: ArrMedia? = null,
        val seerrMedia: RequestMediaDetails? = null,
        val bazarrMedia: BazarrDetails? = null,
        val rtRatings: RottenTomatoesRating? = null,
        val imdbRatings: ImdbRating? = null,
        val seasons: List<SeasonWrapper> = emptyList(),
        val episodes: List<EpisodeWrapper> = emptyList(),
        // Arr-specific additions (Seasons, Albums, etc.)
        val albums: List<ArrAlbum> = emptyList(),
        val tracks: Map<Long, List<LidarrTrack>> = emptyMap(),
        val trackFiles: Map<Long, List<LidarrTrackFile>> = emptyMap(),
        val bookSeries: List<BookSeries> = emptyList(),
        val bookFiles: List<BookFile> = emptyList(),
        val books: List<Book> = emptyList(),
        val extraFiles: List<ExtraFile> = emptyList(),
        val keywords: List<Keyword> = emptyList(),
        val isMonitored: Boolean = false,
        val availableInstances: List<Instance> = emptyList(),
        val selectedInstanceId: Long? = null,
        val instancePresences: List<InstanceMediaPresence> = emptyList(),
        val queueItems: List<QueueItem> = emptyList(),
        val combineSeerrArrMedia: Boolean = true,
        val bazarrDetailsIntegration: Boolean = true,
    ) : UnifiedMediaDetailsUiState {
        val missingInstances: List<Instance>
            get() = instancePresences.filter { !it.isPresent }.map { it.instance }

        val presentInstances: List<Instance>
            get() = instancePresences.filter { it.isPresent }.map { it.instance }

        val hasArrId: Boolean
            get() = arrMedia?.let { it.id != null && it.id != 0L } ?: false

        val hasSeasonsOrFiles: Boolean
            get() = seasons.isNotEmpty() || (hasArrId && arrMedia !is ArrSeries) || queueItems.isNotEmpty()

        val isMovieOrTv: Boolean
            get() =
                arrMedia is ArrMovie ||
                    arrMedia is ArrSeries ||
                    seerrMedia is MovieDetails ||
                    seerrMedia is TvDetails

        val canDeleteFile: Boolean
            get() = canDeleteFile()

        fun canDeleteFile(instanceType: InstanceType? = null): Boolean {
            val type = instanceType ?: when (arrMedia) {
                is ArrMovie -> InstanceType.Radarr
                is Audiobook -> InstanceType.Listenarr
                else -> null
            }
            return when (type) {
                InstanceType.Radarr ->
                    (arrMedia as? ArrMovie)?.let { it.movieFile != null || it.movieFileId != null } == true
                InstanceType.Listenarr ->
                    (arrMedia as? Audiobook)?.let {
                        it.files.isNotEmpty() ||
                            it.fileCount > 0 ||
                            !it.filePath.isNullOrBlank()
                    } == true
                else -> false
            }
        }

        fun hasTracearr(isTracearrConfigured: Boolean): Boolean = isTracearrConfigured && isMovieOrTv

        fun getAvailableTabs(isTracearrConfigured: Boolean): List<UnifiedMediaDetailsTab> =
            buildList {
                if (hasSeasonsOrFiles) add(UnifiedMediaDetailsTab.SeasonsFiles)
                add(UnifiedMediaDetailsTab.Overview)
                if (hasTracearr(isTracearrConfigured)) {
                    add(UnifiedMediaDetailsTab.Analytics)
                    add(UnifiedMediaDetailsTab.History)
                }
            }

        val defaultTab: UnifiedMediaDetailsTab
            get() = if (hasSeasonsOrFiles) UnifiedMediaDetailsTab.SeasonsFiles else UnifiedMediaDetailsTab.Overview

        fun findSeriesAndEpisode(initialEpisodeId: Long): Pair<ArrSeries, Episode>? {
            val series = (arrMedia as? ArrSeries) ?: episodes.firstNotNullOfOrNull { it.arrEpisode?.series }
            val episode = episodes.mapNotNull { it.arrEpisode }.find { it.id == initialEpisodeId }
            return if (series != null && episode != null) series to episode else null
        }

        val displayTitle: String?
            get() = seerrMedia?.displayTitle ?: arrMedia?.title

        val tagline: String?
            get() = seerrMedia?.tagline

        val overview: String?
            get() = seerrMedia?.overview ?: arrMedia?.overview

        val bannerUrl: String?
            get() = seerrMedia?.fullBackdropPath ?: arrMedia?.getBanner()?.remoteUrl

        val posterUrl: String?
            get() = seerrMedia?.fullPosterPath ?: arrMedia?.getPoster()?.remoteUrl

        val clearLogo: String?
            get() = arrMedia?.getClearLogo()?.remoteUrl

        val ratings: List<RatingItem>
            get() {
                val seerrRatings =
                    if (seerrMedia != null) {
                        buildList {
                            rtRatings?.let { rt ->
                                if (rt.criticsScore != null && rt.criticsRating != null) {
                                    add(RatingItem("${rt.criticsScore}%", rt.criticsRating.icon))
                                }
                                if (rt.audienceRating != null && rt.audienceScore != null) {
                                    add(RatingItem("${rt.audienceScore}%", rt.audienceRating.icon))
                                }
                            }
                            imdbRatings?.let { imdb ->
                                add(RatingItem("${(imdb.criticsScore * 10).roundToInt()}%", MR.images.imdb))
                            }
                            add(RatingItem("${(seerrMedia.voteAverage * 10).roundToInt()}%", MR.images.tmdb))
                        }
                    } else {
                        emptyList()
                    }

                val arrRatings = arrMedia?.ratings?.toRatingItems() ?: emptyList()

                val combined = ArrayList(seerrRatings)
                val addedProviders = combined.mapNotNull { it.provider }.toMutableSet()

                for (item in arrRatings) {
                    val prov = item.provider
                    if (prov == null || !addedProviders.contains(prov)) {
                        combined.add(item)
                        if (prov != null) {
                            addedProviders.add(prov)
                        }
                    }
                }
                return combined
            }

        val year: String?
            get() = seerrMedia?.displayDate?.format("yyyy") ?: arrMedia?.year?.toString()

        val runtimeString: String?
            get() = (seerrMedia as? MovieDetails)?.runtime?.formatMinutesAsRuntime() ?: arrMedia?.runtimeString

        val releasedBy: String?
            get() = arrMedia?.releasedBy

        val upcomingDateString: String?
            get() {
                val media = arrMedia ?: return null
                val seerrNextAirDate = (seerrMedia as? TvDetails)?.nextEpisodeToAir?.airDate
                return media.getUpcomingDateString(seerrNextAirDate)
            }

        val seasonCount: Int?
            get() =
                if (seasons.isNotEmpty()) {
                    seasons.count { it.seasonNumber != 0 }.takeIf { it > 0 } ?: seasons.size
                } else {
                    (seerrMedia as? TvDetails)?.numberOfSeasons
                        ?: (arrMedia as? ArrSeries)?.statistics?.seasonCount
                        ?: (seerrMedia as? TvDetails)?.seasons?.count { it.seasonNumber != 0 }
                        ?: (arrMedia as? ArrSeries)?.seasons?.count { it.seasonNumber != 0 }
                }

        val genres: List<String>
            get() = seerrMedia?.genres?.map { it.name } ?: arrMedia?.genres ?: emptyList()

        fun getCertification(countryCode: String): String? = seerrMedia?.getCertification(countryCode) ?: arrMedia?.certification
    }
}

private val RatingItem.provider: String?
    get() =
        when (icon) {
            MR.images.imdb -> "imdb"
            MR.images.tmdb -> "tmdb"
            MR.images.rt_fresh, MR.images.rt_rotten -> "rt_critics"
            MR.images.rt_aud_fresh, MR.images.rt_aud_rotten -> "rt_audience"
            MR.images.trakt -> "trakt"
            else -> null
        }
