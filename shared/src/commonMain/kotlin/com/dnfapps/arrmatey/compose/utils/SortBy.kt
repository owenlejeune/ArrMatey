package com.dnfapps.arrmatey.compose.utils

import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class SortBy(
    val iosIcon: String,
    val resource: StringResource
) {
    Title("textformat", MR.strings.title),
    Year("calendar", MR.strings.year),
    Added("clock.fill", MR.strings.added),
    Rating("star.fill", MR.strings.rating),
    FileSize("opticaldiscdrive.fill", MR.strings.file_size),

    // Movies
    Grabbed("arrow.down.circle.fill", MR.strings.grabbed),
    DigitalRelease("play.tv", MR.strings.digital_release),

    // TV
    NextAiring("clock", MR.strings.next_airing),
    PreviousAiring("clock.arrow.trianglehead.counterclockwise.rotate.90", MR.strings.previous_airing),

    // Lookup
    Relevance("star", MR.strings.relevance),

    // Download Client
    Size("opticaldiscdrive.fill", MR.strings.size),
    Progress("progress.indicator", MR.strings.progress),
    DownloadSpeed("arrow.down.to.line", MR.strings.download_speed),
    UploadSpeed("arrow.up", MR.strings.upload_speed),
    Eta("clock", MR.strings.eta),

    // Prowlarr
    Name("textformat", MR.strings.name),
    Priority("star", MR.strings.priority),
    Protocol("arrow.down.circle", MR.strings.protocol),
    Privacy("hand.raised", MR.strings.privacy);

    companion object {

        private val sonarrOps by lazy {
            listOf(Title, Year, Added, Rating, FileSize, NextAiring, PreviousAiring)
        }
        private val radarrOps by lazy {
            listOf(Title, Year, Added, Rating, FileSize, Grabbed, DigitalRelease)
        }
        private val lidarrOps by lazy {
            listOf(Title, Year, Added, Rating, FileSize)
        }
        private val prowlarrOps by lazy {
            listOf(Name, Added, Protocol, Priority, Privacy)
        }

        fun typeEntries(type: InstanceType) =
            when (type) {
                InstanceType.Sonarr -> sonarrOps
                InstanceType.Radarr -> radarrOps
                InstanceType.Lidarr -> lidarrOps
                InstanceType.Prowlarr -> prowlarrOps
                else -> emptyList()
            }

        fun lookupEntries() = listOf(Relevance, Year, Rating)

        fun downloadClientEntries() = listOf(Title, Added, Size, Progress, DownloadSpeed, UploadSpeed, Eta)
    }
}

enum class SortOrder(
    val iosIcon: String,
    val resource: StringResource
) {
    Asc("arrow.up", MR.strings.sort_ascending),
    Desc("arrow.down", MR.strings.sort_descending)
}
