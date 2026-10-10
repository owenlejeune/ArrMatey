package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.added
import com.dnfapps.arrmatey.shared.audiobook_file_added
import com.dnfapps.arrmatey.shared.audiobook_file_deleted
import com.dnfapps.arrmatey.shared.book_file_deleted
import com.dnfapps.arrmatey.shared.book_file_imported
import com.dnfapps.arrmatey.shared.book_file_renamed
import com.dnfapps.arrmatey.shared.download_failed
import com.dnfapps.arrmatey.shared.download_folder_imported
import com.dnfapps.arrmatey.shared.download_ignored
import com.dnfapps.arrmatey.shared.episode_file_deleted
import com.dnfapps.arrmatey.shared.episode_file_renamed
import com.dnfapps.arrmatey.shared.grabbed
import com.dnfapps.arrmatey.shared.movie_file_deleted
import com.dnfapps.arrmatey.shared.movie_file_renamed
import com.dnfapps.arrmatey.shared.movie_folder_imported
import com.dnfapps.arrmatey.shared.series_folder_imported
import com.dnfapps.arrmatey.shared.unknown
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.SerialName

enum class HistoryEventType(
    val resource: StringResource,
) {
    Unknown(MR.strings.unknown),

    @SerialName("grabbed")
    Grabbed(MR.strings.grabbed),

    @SerialName("downloadFolderImported")
    DownloadFolderImported(MR.strings.download_folder_imported),

    @SerialName("downloadFailed")
    DownloadFailed(MR.strings.download_failed),

    @SerialName("downloadIgnored")
    DownloadIgnored(MR.strings.download_ignored),

    @SerialName("movieFileRenamed")
    MovieFileRenamed(MR.strings.movie_file_renamed),

    @SerialName("movieFileDeleted")
    MovieFileDeleted(MR.strings.movie_file_deleted),

    @SerialName("movieFolderImported")
    MovieFolderImported(MR.strings.movie_folder_imported),

    @SerialName("episodeFileRenamed")
    EpisodeFileRenamed(MR.strings.episode_file_renamed),

    @SerialName("episodeFileDeleted")
    EpisodeFileDeleted(MR.strings.episode_file_deleted),

    @SerialName("seriesFolderImported")
    SeriesFolderImported(MR.strings.series_folder_imported),

    @SerialName("bookFileImported")
    BookFileImported(MR.strings.book_file_imported),

    @SerialName("bookFileRenamed")
    BookFileRenamed(MR.strings.book_file_renamed),

    @SerialName("bokFileDeleted")
    BookFileDelete(MR.strings.book_file_deleted),

    @SerialName("Added")
    Added(MR.strings.added),

    @SerialName("File Removed")
    AudiobookFileRemoved(MR.strings.audiobook_file_deleted),

    @SerialName("File Added")
    AudiobookFileAdded(MR.strings.audiobook_file_added),

    @SerialName("downloadImported")
    DownloadImported(MR.strings.download_imported),

    @SerialName("trackFileImported")
    TrackFileImported(MR.strings.track_file_imported),

    @SerialName("trackFileDeleted")
    TrackFileDelete(MR.strings.track_file_deleted),

    @SerialName("albumImportIncomplete")
    AlbumImportIncomplete(MR.strings.album_import_incomplete),
}
