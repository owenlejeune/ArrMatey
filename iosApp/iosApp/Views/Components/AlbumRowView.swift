//
//  AlbumRowView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-12.
//

import SwiftUI
import Shared

struct AlbumRowView: View {
    let artist: Arrtist
    let album: ArrAlbum
    let tracks: [LidarrTrack]
    let trackFiles: [LidarrTrackFile]
    let onToggleAlbumMonitor: (ArrAlbum) -> Void
    let onAlbumAutomaticSearch: (Int64) -> Void
    let automaticSearchIds: Set<Int64>
    let onDeleteAlbum: (ArrAlbum) -> Void
    let albumDeleteInProgress: Bool
    
    @State private var expanded: Bool = false
    
    @EnvironmentObject private var navigation: NavigationManager
    
    private var titleLabel: String {
        var result = album.title ?? MR.strings().unknown.localized()
        if let year = album.releaseDate?.format(pattern: "YYYY"),
           let title = album.title,
           !title.contains(String(describing: year)) {
            result += " (\(year))"
        }
        return result
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            albumHeader
            
            if expanded {
                albumDetails
                
                HStack(spacing: 6) {
                    Button(action: { onDeleteAlbum(album) }) {
                        if albumDeleteInProgress {
                            ProgressView().progressViewStyle(.circular)
                        } else {
                            Image(systemName: "trash")
                        }
                    }
                    .tint(.red)
                    .buttonStyle(.borderedProminent)
                    .controlSize(.regular)
                    
                    ReleaseDownloadButtons(
                        onInteractiveClicked: {
                            if let artistId = artist.id?.int64Value {
                                let route: MediaRoute = .albumReleases(albumId: album.id, artistId: artistId)
                                navigation.go(to: route, of: .lidarr)
                            }
                        },
                        automaticSearchEnabled: album.monitored,
                        onAutomaticClicked: {
                            onAlbumAutomaticSearch(album.id)
                        },
                        automaticSearchInProgress: automaticSearchIds.contains(album.id)
                    )
                }
                
                ForEach(Array(tracks.enumerated()), id: \.element.id) { index, track in
                    TrackRow(
                        track: track,
                        trackFile: trackFiles.first(where: { $0.albumId == album.id })
                    )
                    
                    if track != tracks.last {
                        Divider()
                    }
                }
            }
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 10, style: .continuous)
                .fill(Color(.systemGroupedBackground))
        )
        .animation(.easeInOut(duration: 0.3), value: expanded)
    }
    
    private var albumHeader: some View {
        HStack(alignment: .center, spacing: 12) {
            HStack(alignment: .center, spacing: 12) {
                AlbumCoverView(album: album)
                    .frame(width: 60, height: 60)
                
                VStack(alignment: .leading, spacing: 0) {
                    Text(titleLabel)
                        .font(.system(size: 16, weight: .medium))
                        .lineLimit(2)
                    
                    Text(trackStats)
                        .font(.system(size: 14))
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                Image(systemName: "chevron.down.circle.fill")
                    .rotationEffect(.degrees(expanded ? 180 : 0))
            }
            .onTapGesture { expanded.toggle() }
            
            Image(systemName: album.monitored ? "bookmark.fill" : "bookmark")
                .onTapGesture {
                    onToggleAlbumMonitor(album)
                }
        }
    }
    
    private var albumDetails: some View {
        VStack(alignment: .leading) {
            Text(infoString)
                .font(.system(size: 16))
                .foregroundColor(.secondary)
                .multilineTextAlignment(.leading)
        }
        .padding(.vertical, 4)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
    
    
    private var trackStats: String {
        if let stats = album.statistics {
            return "\(stats.trackFileCount)/\(stats.totalTrackCount)"
        }
        return ""
    }
    
    private var releaseDate: String {
        album.releaseDate?.format(pattern: "MMM d, yyyy")
        ?? MR.strings().tba.localized()
    }
    
    private var runtime: String {
        let totalMs = tracks.reduce(0) { $0 + $1.duration }
        return (Int(totalMs) / 60_000).formatAsRuntime()
    }
    
    private var infoString: String {
        [releaseDate, runtime, album.statistics?.sizeOnDisk.bytesAsFileSizeString()]
            .compactMap { $0 }
            .joined(separator: " • ")
    }
}
