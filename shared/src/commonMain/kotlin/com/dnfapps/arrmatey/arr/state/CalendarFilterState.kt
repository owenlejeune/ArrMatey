package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

@Serializable
data class CalendarFilterState(
    val viewMode: CalendarViewMode = CalendarViewMode.List,
    val contentFilter: ContentFilter = ContentFilter.All,
    val showMonitoredOnly: Boolean = false,
    val showPremiersOnly: Boolean = false,
    val showFinalesOnly: Boolean = false,
) {
    constructor() : this(CalendarViewMode.List, ContentFilter.All, false, false, false)
}

@Serializable
enum class CalendarViewMode {
    List,
    Month,
}

@Serializable
enum class ContentFilter(
    val resource: StringResource,
    val systemImage: String,
    val instanceType: InstanceType? = null,
) {
    All(MR.strings.all, "play.square.stack"),
    MoviesOnly(MR.strings.movies, "movieclapper", InstanceType.Radarr),
    EpisodesOnly(MR.strings.episodes, "tv", InstanceType.Sonarr),
    AlbumsOnly(MR.strings.albums_header, "music.note", InstanceType.Lidarr),
    BooksOnly(MR.strings.books, "book", InstanceType.Bookshelf),
    AudiobooksOnly(MR.strings.audiobooks, "headphones", InstanceType.Listenarr),
}
