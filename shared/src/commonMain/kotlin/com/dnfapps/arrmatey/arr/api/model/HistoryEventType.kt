package com.dnfapps.arrmatey.arr.api.model

import com.dnfapps.arrmatey.shared.*
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = HistoryEventTypeSerializer::class)
enum class HistoryEventType(
    val resource: StringResource,
) {
    Unknown(MR.strings.unknown),

    // General / Download Events
    Grabbed(MR.strings.grabbed),
    DownloadFolderImported(MR.strings.download_folder_imported),
    DownloadFailed(MR.strings.download_failed),
    DownloadIgnored(MR.strings.download_ignored),
    DownloadImported(MR.strings.download_imported),
    AlbumImportIncomplete(MR.strings.album_import_incomplete),

    // Movie Events (Radarr)
    MovieFileImported(MR.strings.movie_file_imported),
    MovieFileAdded(MR.strings.audiobook_file_added),
    MovieFileRenamed(MR.strings.movie_file_renamed),
    MovieFileDeleted(MR.strings.movie_file_deleted),
    MovieFolderImported(MR.strings.movie_folder_imported),
    MovieDeleted(MR.strings.deleted),

    // Series / Episode Events (Sonarr)
    SeriesFolderImported(MR.strings.series_folder_imported),
    EpisodeFileAdded(MR.strings.audiobook_file_added),
    EpisodeFileRenamed(MR.strings.episode_file_renamed),
    EpisodeFileDeleted(MR.strings.episode_file_deleted),
    SeriesDeleted(MR.strings.deleted),

    // Book / Author Events (Bookshelf / Readarr / Chaptarr)
    BookFileImported(MR.strings.book_file_imported),
    BookFileAdded(MR.strings.audiobook_file_added),
    BookFileRenamed(MR.strings.book_file_renamed),
    BookFileDeleted(MR.strings.book_file_deleted),
    BookFileDelete(MR.strings.book_file_deleted),
    BookFolderImported(MR.strings.series_folder_imported),
    BookDeleted(MR.strings.deleted),
    AuthorDeleted(MR.strings.deleted),

    // Music / Artist Events (Lidarr)
    TrackFileImported(MR.strings.track_file_imported),
    TrackFileAdded(MR.strings.audiobook_file_added),
    TrackFileDelete(MR.strings.track_file_deleted),
    AlbumFolderImported(MR.strings.series_folder_imported),
    ArtistFolderImported(MR.strings.series_folder_imported),
    AlbumDeleted(MR.strings.deleted),
    ArtistDeleted(MR.strings.deleted),

    // Audiobook / Listenarr Events
    Added(MR.strings.added),
    AudiobookFileAdded(MR.strings.audiobook_file_added),
    AudiobookFileRemoved(MR.strings.audiobook_file_deleted),
    Moved(MR.strings.history_state_renamed),
}

object HistoryEventTypeSerializer : KSerializer<HistoryEventType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HistoryEventType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): HistoryEventType {
        val value = decoder.decodeString()
        return when (value.lowercase()) {
            "unknown" -> HistoryEventType.Unknown
            "grabbed" -> HistoryEventType.Grabbed
            "downloadfolderimported" -> HistoryEventType.DownloadFolderImported
            "downloadfailed" -> HistoryEventType.DownloadFailed
            "downloadignored" -> HistoryEventType.DownloadIgnored
            "downloadimported" -> HistoryEventType.DownloadImported
            "albumimportincomplete" -> HistoryEventType.AlbumImportIncomplete
            "moviefileimported" -> HistoryEventType.MovieFileImported
            "moviefileadded" -> HistoryEventType.MovieFileAdded
            "moviefilerenamed" -> HistoryEventType.MovieFileRenamed
            "moviefiledeleted" -> HistoryEventType.MovieFileDeleted
            "moviefolderimported" -> HistoryEventType.MovieFolderImported
            "moviedeleted" -> HistoryEventType.MovieDeleted
            "seriesfolderimported" -> HistoryEventType.SeriesFolderImported
            "episodefileadded" -> HistoryEventType.EpisodeFileAdded
            "episodefilerenamed" -> HistoryEventType.EpisodeFileRenamed
            "episodefiledeleted" -> HistoryEventType.EpisodeFileDeleted
            "seriesdeleted" -> HistoryEventType.SeriesDeleted
            "bookfileimported" -> HistoryEventType.BookFileImported
            "bookfileadded" -> HistoryEventType.BookFileAdded
            "bookfilerenamed" -> HistoryEventType.BookFileRenamed
            "bookfiledeleted", "bokfiledeleted" -> HistoryEventType.BookFileDeleted
            "bookfolderimported" -> HistoryEventType.BookFolderImported
            "bookdeleted" -> HistoryEventType.BookDeleted
            "authordeleted" -> HistoryEventType.AuthorDeleted
            "trackfileimported" -> HistoryEventType.TrackFileImported
            "trackfileadded" -> HistoryEventType.TrackFileAdded
            "trackfiledeleted" -> HistoryEventType.TrackFileDelete
            "albumfolderimported" -> HistoryEventType.AlbumFolderImported
            "artistfolderimported" -> HistoryEventType.ArtistFolderImported
            "albumdeleted" -> HistoryEventType.AlbumDeleted
            "artistdeleted" -> HistoryEventType.ArtistDeleted
            "added" -> HistoryEventType.Added
            "file added", "audiobookfileadded" -> HistoryEventType.AudiobookFileAdded
            "file removed", "audiobookfileremoved" -> HistoryEventType.AudiobookFileRemoved
            "moved" -> HistoryEventType.Moved
            else -> HistoryEventType.Unknown
        }
    }

    override fun serialize(encoder: Encoder, value: HistoryEventType) {
        encoder.encodeString(value.name)
    }
}
