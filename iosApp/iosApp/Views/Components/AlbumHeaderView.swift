//
//  AlbumHeaderView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-12.
//

import SwiftUI
import Shared

struct AlbumHeaderView: View {
    let artistId: Int64?
    let album: ArrAlbum
    let tracks: [LidarrTrack]
    let onAutomaticSearch: () -> Void
    let isSearching: Bool
    let onDelete: () -> Void
    let deleteInProgress: Bool
    
    // Assuming these are injected or provided via a Parent
    // let navigation = ...

    private var infoString: String {
        let release = album.releaseDate?.format(pattern: "MMM d, yyyy")
            ?? MR.strings().tba.localized()
        
        let totalDurationMs = tracks.reduce(0) { $0 + ($1.duration) }
        let runtime = (Int(totalDurationMs) / 60_000).formatAsRuntime()
        
        var parts = [release, runtime]
        
        if let size = album.statistics?.sizeOnDisk {
            parts.append(size.bytesAsFileSizeString())
        }
        
        return parts.joined(separator: " • ")
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(infoString)
                .font(.system(size: 16))
                .foregroundColor(.secondary)
            
            HStack(spacing: 12) {
                // Interactive Search / Release button
                Button(action: {
                    // Navigation logic here
                }) {
                    Label("Releases", systemImage: "list.bullet")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.bordered)
                
                // Automatic Search Button
                Button(action: onAutomaticSearch) {
                    if isSearching {
                        ProgressView().controlSize(.small)
                    } else {
                        Image(systemName: "magnifyingglass")
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(!album.monitored || isSearching)

                // Delete Button
                Button(role: .destructive, action: onDelete) {
                    if deleteInProgress {
                        ProgressView().controlSize(.small)
                    } else {
                        Image(systemName: "trash")
                    }
                }
                .buttonStyle(.bordered)
                .disabled(deleteInProgress)
            }
            .frame(maxWidth: .infinity)
        }
    }
}
