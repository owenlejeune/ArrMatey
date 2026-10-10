package com.dnfapps.arrmatey.compose

import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.activity
import com.dnfapps.arrmatey.shared.audiobooks
import com.dnfapps.arrmatey.shared.bazarr
import com.dnfapps.arrmatey.shared.books
import com.dnfapps.arrmatey.shared.custom_webpage
import com.dnfapps.arrmatey.shared.dashboard
import com.dnfapps.arrmatey.shared.discover
import com.dnfapps.arrmatey.shared.downloads
import com.dnfapps.arrmatey.shared.library
import com.dnfapps.arrmatey.shared.movies
import com.dnfapps.arrmatey.shared.music
import com.dnfapps.arrmatey.shared.prowlarr
import com.dnfapps.arrmatey.shared.requests
import com.dnfapps.arrmatey.shared.schedule
import com.dnfapps.arrmatey.shared.series
import com.dnfapps.arrmatey.shared.settings
import com.dnfapps.arrmatey.shared.tracearr
import dev.icerock.moko.resources.StringResource

sealed interface TabItem {
    val iosIcon: String
    val resource: StringResource
    val isDisabled: Boolean
    val associatedTypes: List<InstanceType>
    val associatedType: InstanceType?
        get() = associatedTypes.firstOrNull()

    val key: String

    data object Settings : TabItem {
        override val iosIcon: String
            get() = "gear"
        override val resource: StringResource
            get() = MR.strings.settings
        override val isDisabled: Boolean
            get() = false
        override val associatedTypes: List<InstanceType>
            get() = emptyList()
        override val key: String
            get() = "settings"
    }

    enum class Standard(
        override val iosIcon: String,
        override val resource: StringResource,
        override val isDisabled: Boolean = false,
        override val associatedTypes: List<InstanceType> = emptyList(),
    ) : TabItem {
        LIBRARY("books.vertical", MR.strings.library),
        SHOWS("tv", MR.strings.series, associatedTypes = listOf(InstanceType.Sonarr)),
        MOVIES("movieclapper", MR.strings.movies, associatedTypes = listOf(InstanceType.Radarr)),
        MUSIC("music.quarternote.3", MR.strings.music, associatedTypes = listOf(InstanceType.Lidarr)),
        BOOKS("book", MR.strings.books, associatedTypes = listOf(InstanceType.Bookshelf, InstanceType.Chaptarr)),
        AUDIOBOOKS("book.closed", MR.strings.audiobooks, associatedTypes = listOf(InstanceType.Listenarr, InstanceType.Chaptarr)),
        COMICS("laser.burst", MR.strings.comics, associatedTypes = listOf(InstanceType.Kapowarr)),
        ACTIVITY("square.and.arrow.down", MR.strings.activity),
        DOWNLOADS("arrow.down.circle", MR.strings.downloads),
        CALENDAR("calendar", MR.strings.schedule),
        REQUESTS("tray.fill", MR.strings.requests),
        DISCOVER("sparkles", MR.strings.discover),
        PROWLARR("magnifyingglass.circle", MR.strings.prowlarr, associatedTypes = listOf(InstanceType.Prowlarr)),
        BAZARR("captions.bubble", MR.strings.bazarr, associatedTypes = listOf(InstanceType.Bazarr)),
        DASHBOARD("rectangle.grid.3x1", MR.strings.dashboard),
        TRACEARR("tv.badge.wifi", MR.strings.tracearr, associatedTypes = listOf(InstanceType.Tracearr)),
        ;

        override val key: String get() = "standard_$name"
    }

    data class CustomWebpage(
        val id: Long,
        val name: String,
        val url: String,
        val headers: List<InstanceHeader> = emptyList(),
    ) : TabItem {
        override val iosIcon: String = "globe"
        override val resource: StringResource = MR.strings.custom_webpage // Will use name instead
        override val isDisabled: Boolean = false
        override val associatedTypes: List<InstanceType> = emptyList()
        override val key: String = "webpage_$id"
    }

    companion object {
        fun standardEntries(): List<Standard> = Standard.entries.filter { !it.isDisabled }

        fun defaultStandardEntries(): List<Standard> = listOf(
            Standard.DASHBOARD,
            Standard.LIBRARY,
            Standard.DISCOVER,
            Standard.ACTIVITY,
            Standard.CALENDAR,
        )

        fun defaultHiddenStandard(): List<Standard> = standardEntries().filter { !defaultStandardEntries().contains(it) }

        fun defaultStandardKeys(): List<String> = defaultStandardEntries().map { it.key }

        fun defaultHiddenKeys(): List<String> = defaultHiddenStandard().map { it.key }
    }
}
