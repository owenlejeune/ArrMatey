package com.dnfapps.arrmatey.compose

import com.dnfapps.arrmatey.instances.model.InstanceHeader
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

sealed interface TabItem {
    val iosIcon: String
    val resource: StringResource
//    val drawerOnly: Boolean
    val isDisabled: Boolean
    val associatedType: InstanceType?

    val key: String

    data object Settings: TabItem {
        override val iosIcon: String
            get() = "gear"
        override val resource: StringResource
            get() = MR.strings.settings
        override val isDisabled: Boolean
            get() = false
        override val associatedType: InstanceType?
            get() = null
        override val key: String
            get() = "settings"
    }

    enum class Standard(
        override val iosIcon: String,
        override val resource: StringResource,
//        override val drawerOnly: Boolean = false,
        override val isDisabled: Boolean = false,
        override val associatedType: InstanceType? = null
    ) : TabItem {
        SHOWS("tv", MR.strings.series, associatedType = InstanceType.Sonarr),
        MOVIES("movieclapper", MR.strings.movies, associatedType = InstanceType.Radarr),
        MUSIC("music.quarternote.3", MR.strings.music, associatedType = InstanceType.Lidarr),
        ACTIVITY("square.and.arrow.down", MR.strings.activity),
        DOWNLOADS("arrow.down.circle", MR.strings.downloads),
        CALENDAR("calendar", MR.strings.schedule),
        REQUESTS("tray.fill", MR.strings.requests, isDisabled = true),
        PROWLARR("magnifyingglass.circle", MR.strings.prowlarr, associatedType = InstanceType.Prowlarr);
//        SETTINGS("gear", MR.strings.settings, drawerOnly = true);

        override val key: String get() = "standard_${name}"
    }

    data class CustomWebpage(
        val id: Long,
        val name: String,
        val url: String,
        val headers: List<InstanceHeader> = emptyList()
    ) : TabItem {
        override val iosIcon: String = "globe"
        override val resource: StringResource = MR.strings.custom_webpage // Will use name instead
//        override val drawerOnly: Boolean = false
        override val isDisabled: Boolean = false
        override val associatedType: InstanceType? = null
        override val key: String = "webpage_$id"
    }

    companion object {
        fun standardEntries(): List<Standard> = Standard.entries.filter { !it.isDisabled }

        fun defaultStandardEntries(): List<Standard> = listOf(
            Standard.SHOWS,
            Standard.MOVIES,
            Standard.MUSIC,
            Standard.ACTIVITY,
            Standard.CALENDAR
        )

        fun defaultHiddenStandard(): List<Standard> =
            standardEntries().filter { !defaultStandardEntries().contains(it) }

        fun defaultStandardKeys(): List<String> = defaultStandardEntries().map { it.key }

        fun defaultHiddenKeys(): List<String> = defaultHiddenStandard().map { it.key }
    }
}