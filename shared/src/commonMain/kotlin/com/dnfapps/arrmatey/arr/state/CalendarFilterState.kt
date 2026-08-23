package com.dnfapps.arrmatey.arr.state

import androidx.compose.ui.graphics.vector.ImageVector
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

data class CalendarFilterState(
    val viewMode: CalendarViewMode = CalendarViewMode.List,
    val contentFilter: ContentFilter = ContentFilter.All,
    val showMonitoredOnly: Boolean = false,
    val showPremiersOnly: Boolean = false,
    val showFinalesOnly: Boolean = false,
    val instanceId: Long? = null
) {
    constructor(): this(CalendarViewMode.List, ContentFilter.All, false, false, false, null)
}

enum class CalendarViewMode {
    List, Month
}

enum class ContentFilter(
    val resource: StringResource,
    val systemImage: String
) {
    All(MR.strings.all, "play.square.stack"),
    MoviesOnly(MR.strings.movies, "movieclapper"),
    EpisodesOnly(MR.strings.episodes, "tv"),
    AlbumsOnly(MR.strings.albums_header, "music.note"),
    BooksOnly(MR.strings.books, "book"),
    AudiobooksOnly(MR.strings.audiobooks, "headphones")
}