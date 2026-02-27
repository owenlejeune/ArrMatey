package com.dnfapps.arrmatey.compose

import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class TabItem(
    val iosIcon: String,
    val resource: StringResource,
    val drawerOnly: Boolean = false,
    val isDisabled: Boolean = false, // should only be set for in-progress views
    val associatedType: InstanceType? = null
) {
    SHOWS("tv", MR.strings.series, associatedType = InstanceType.Sonarr),
    MOVIES("movieclapper", MR.strings.movies, associatedType = InstanceType.Radarr),
    MUSIC("music.quarternote.3", MR.strings.music, associatedType = InstanceType.Lidarr),
    ACTIVITY("square.and.arrow.down", MR.strings.activity),
    CALENDAR("calendar", MR.strings.schedule),
    REQUESTS("tray.fill", MR.strings.requests),

    SETTINGS("gear", MR.strings.settings, drawerOnly = true);

    companion object {
        val navigationEntries = entries.filter { !it.drawerOnly }
        val defaultEntries = listOf(SHOWS, MOVIES, MUSIC, ACTIVITY, CALENDAR)
        val defaultHidden = navigationEntries.filter { !defaultEntries.contains(it) }
    }
}