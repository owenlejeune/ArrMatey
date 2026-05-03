package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.SerialName

enum class HistoryEventType(val resource: StringResource) {
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
    BookFileDelete(MR.strings.book_file_deleted)
}