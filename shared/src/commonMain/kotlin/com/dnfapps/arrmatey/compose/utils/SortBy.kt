package com.dnfapps.arrmatey.compose.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector
import com.dnfapps.arrmatey.compose.icons.Hard_drive
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class SortBy(
    val iosIcon: String,
    val androidIcon: ImageVector,
    val resource: StringResource
) {
    Title("textformat", Icons.Default.SortByAlpha, MR.strings.title),
    Year("calendar", Icons.Default.CalendarMonth, MR.strings.year),
    Added("clock.fill", Icons.Filled.Schedule, MR.strings.added),
    Rating("star.fill", Icons.Filled.Star, MR.strings.rating),
    FileSize("opticaldiscdrive.fill", Hard_drive, MR.strings.file_size),

    // Movies
    Grabbed("arrow.down.circle.fill", Icons.Default.ArrowCircleDown, MR.strings.grabbed),
    DigitalRelease("play.tv", Icons.Default.Tv, MR.strings.digital_release),

    // TV
    NextAiring("clock", Icons.Default.Schedule, MR.strings.next_airing),
    PreviousAiring("clock.arrow.trianglehead.counterclockwise.rotate.90", Icons.Default.History, MR.strings.previous_airing),

    // Lookup
    Relevance("star", Icons.Default.Star, MR.strings.relevance);

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

        fun typeEntries(type: InstanceType) =
            when (type) {
                InstanceType.Sonarr -> sonarrOps
                InstanceType.Radarr -> radarrOps
                InstanceType.Lidarr -> lidarrOps
                InstanceType.Prowlarr -> emptyList()
            }

        fun lookupEntries() = listOf(Relevance, Year, Rating)
    }
}

enum class SortOrder(
    val iosIcon: String,
    val androidIcon: ImageVector,
    val resource: StringResource
) {
    Asc("arrow.up", Icons.Default.ArrowUpward, MR.strings.sort_ascending),
    Desc("arrow.down", Icons.Default.ArrowDownward, MR.strings.sort_descending)
}
