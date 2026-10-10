package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.all
import com.dnfapps.arrmatey.shared.grabbed
import com.dnfapps.arrmatey.shared.history_state_deleted
import com.dnfapps.arrmatey.shared.history_state_failed
import com.dnfapps.arrmatey.shared.history_state_ignored
import com.dnfapps.arrmatey.shared.history_state_imported
import com.dnfapps.arrmatey.shared.history_state_renamed
import dev.icerock.moko.resources.StringResource

enum class HistoryStateFilter(
    val resource: StringResource,
) {
    All(MR.strings.all),
    Grabbed(MR.strings.grabbed),
    Imported(MR.strings.history_state_imported),
    Failed(MR.strings.history_state_failed),
    Deleted(MR.strings.history_state_deleted),
    Renamed(MR.strings.history_state_renamed),
    Ignored(MR.strings.history_state_ignored),
    ;

    fun matches(eventType: HistoryEventType): Boolean = when (this) {
        All -> true
        Grabbed -> eventType == HistoryEventType.Grabbed
        Imported ->
            eventType in
                listOf(
                    HistoryEventType.DownloadFolderImported,
                    HistoryEventType.MovieFolderImported,
                    HistoryEventType.SeriesFolderImported,
                    HistoryEventType.BookFileImported,
                    HistoryEventType.MovieFileImported,
                    HistoryEventType.TrackFileImported,
                    HistoryEventType.BookFileAdded,
                    HistoryEventType.MovieFileAdded,
                    HistoryEventType.TrackFileAdded,
                    HistoryEventType.EpisodeFileAdded,
                    HistoryEventType.AudiobookFileAdded,
                    HistoryEventType.Added,
                    HistoryEventType.DownloadImported,
                )
        Failed -> eventType == HistoryEventType.DownloadFailed
        Deleted ->
            eventType in
                listOf(
                    HistoryEventType.MovieFileDeleted,
                    HistoryEventType.EpisodeFileDeleted,
                    HistoryEventType.BookFileDelete,
                    HistoryEventType.BookFileDeleted,
                    HistoryEventType.TrackFileDelete,
                    HistoryEventType.AudiobookFileRemoved,
                    HistoryEventType.MovieDeleted,
                    HistoryEventType.SeriesDeleted,
                    HistoryEventType.BookDeleted,
                    HistoryEventType.AuthorDeleted,
                    HistoryEventType.AlbumDeleted,
                    HistoryEventType.ArtistDeleted,
                )
        Renamed ->
            eventType in
                listOf(
                    HistoryEventType.MovieFileRenamed,
                    HistoryEventType.EpisodeFileRenamed,
                    HistoryEventType.BookFileRenamed,
                    HistoryEventType.Moved,
                )
        Ignored -> eventType == HistoryEventType.DownloadIgnored
    }
}
