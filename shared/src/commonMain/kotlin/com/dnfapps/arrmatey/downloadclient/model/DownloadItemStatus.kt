package com.dnfapps.arrmatey.downloadclient.model

import com.dnfapps.arrmatey.downloadclient.api.model.DelugeTorrentState
import com.dnfapps.arrmatey.downloadclient.api.model.QBittorrentTorrentState
import com.dnfapps.arrmatey.downloadclient.api.model.SABnzbdQueueSlotStatus
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource

enum class DownloadItemStatus(val resource: StringResource) {
    Error(MR.strings.torrent_status_error),
    MissingFiles(MR.strings.torrent_status_missing_files),
    Uploading(MR.strings.torrent_status_seeding),
    UploadingPaused(MR.strings.torrent_status_completed),
    Queued(MR.strings.torrent_status_queued),
    UploadingForced(MR.strings.torrent_status_force_seeding),
    Checking(MR.strings.torrent_status_checking),
    CheckingResumeData(MR.strings.torrent_status_checking_resume_data),
    Downloading(MR.strings.torrent_status_downloading),
    DownloadingMetadataForced(MR.strings.torrent_status_force_downloading_metadata),
    DownloadingPaused(MR.strings.torrent_status_paused),
    DownloadingStalled(MR.strings.torrent_status_stalled),
    DownloadingForced(MR.strings.torrent_status_force_downloading),
    Moving(MR.strings.torrent_status_moving),
    Allocating(MR.strings.torrent_status_allocating),
    Propagating(MR.strings.torrent_status_propagating),
    Fetching(MR.strings.torrent_status_fetching),
    Unknown(MR.strings.torrent_status_unknown);

    val isPaused: Boolean
        get() = this == DownloadingPaused || this == UploadingPaused

    companion object {
        fun from(state: QBittorrentTorrentState) = when(state) {
            QBittorrentTorrentState.Error -> Error
            QBittorrentTorrentState.MissingFiles -> MissingFiles
            QBittorrentTorrentState.Uploading,
            QBittorrentTorrentState.UploadingStalled-> Uploading
            QBittorrentTorrentState.UploadingPaused,
            QBittorrentTorrentState.UploadingStopped -> UploadingPaused
            QBittorrentTorrentState.UploadingQueued,
            QBittorrentTorrentState.DownloadingQueued -> Queued
            QBittorrentTorrentState.UploadingChecking,
            QBittorrentTorrentState.DownloadingChecking-> Checking
            QBittorrentTorrentState.UploadingForced -> UploadingForced
            QBittorrentTorrentState.Downloading -> Downloading
            QBittorrentTorrentState.DownloadingMetadata -> DownloadingMetadataForced
            QBittorrentTorrentState.DownloadingPaused,
            QBittorrentTorrentState.DownloadingStopped -> DownloadingPaused
            QBittorrentTorrentState.DownloadingStalled -> DownloadingStalled
            QBittorrentTorrentState.DownloadingForced -> DownloadingForced
            QBittorrentTorrentState.CheckingResumeData -> CheckingResumeData
            QBittorrentTorrentState.Moving -> Moving
            QBittorrentTorrentState.Allocating -> Allocating
            else -> Unknown
        }

        fun from(state: DelugeTorrentState) = when(state) {
            DelugeTorrentState.Downloading -> Downloading
            DelugeTorrentState.Seeding -> Uploading
            DelugeTorrentState.Paused -> DownloadingPaused
            DelugeTorrentState.Queued -> Queued
            DelugeTorrentState.Checking -> Checking
            DelugeTorrentState.Error -> Error
            DelugeTorrentState.Allocating -> Allocating
            DelugeTorrentState.Moving -> Moving
            DelugeTorrentState.Unknown -> Unknown
        }

        fun from(state: SABnzbdQueueSlotStatus) = when(state) {
            SABnzbdQueueSlotStatus.Downloading -> Downloading
            SABnzbdQueueSlotStatus.Queued -> Queued
            SABnzbdQueueSlotStatus.Paused -> DownloadingPaused
            SABnzbdQueueSlotStatus.Propagating -> Propagating
            SABnzbdQueueSlotStatus.Fetching -> Fetching
            SABnzbdQueueSlotStatus.Unknown -> Unknown
        }
    }
}
