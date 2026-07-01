//
//  ArtistFilesView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-12.
//

import SwiftUI
import Shared

struct ArtistFilesView: View {
    let artist: Arrtist
    let albums: [ArrAlbum]
    let tracks: [KotlinLong: [LidarrTrack]]
    let trackFiles: [KotlinLong: [LidarrTrackFile]]
    let searchIds: Set<Int64>
    let onToggleAlbumMonitor: (ArrAlbum) -> Void
    let onEditAlbum: (ArrAlbum) -> Void
    let onAlbumAutomaticSearch: (Int64) -> Void
    let deleteAlbumFiles: (ArrAlbum) -> Void
    let albumDeleteInProgress: Bool
    
    @ObservedObject private var activityQueueViewModel = ActivityQueueViewModelS()
    
    private var queueItems: [QueueItem] {
        activityQueueViewModel.queueItems
    }
    
    var body: some View {
        Section {
            ForEach(albums, id: \.id) { album in
                AlbumRowView(
                    artist: artist,
                    album: album,
                    tracks: tracks[album.id.asKotlinLong] ?? [],
                    trackFiles: trackFiles[album.id.asKotlinLong] ?? [],
                    onToggleAlbumMonitor: onToggleAlbumMonitor,
                    onEditAlbum: onEditAlbum,
                    onAlbumAutomaticSearch: onAlbumAutomaticSearch,
                    automaticSearchIds: searchIds,
                    onDeleteAlbum: deleteAlbumFiles,
                    albumDeleteInProgress: albumDeleteInProgress
                )
            }
        } header: {
            Text(MR.strings().albums_header.localized())
                .font(.system(size: 26, weight: .medium))
        }
    }
}
