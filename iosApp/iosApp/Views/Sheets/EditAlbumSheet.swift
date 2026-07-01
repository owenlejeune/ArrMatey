//
//  EditAlbumSheet.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-07-01.
//

import SwiftUI
import Shared

struct EditAlbumSheet: View {
    let album: ArrAlbum
    let editInProgress: Bool
    let onEditAlbum: (ArrAlbum) -> Void
    
    @State private var monitored: Bool
    @State private var anyReleaseOk: Bool
    @State private var selectedRelease: AlbumRelease?
    
    init(album: ArrAlbum, editInProgress: Bool, onEditAlbum: @escaping (ArrAlbum) -> Void) {
        self.album = album
        self.editInProgress = editInProgress
        self.onEditAlbum = onEditAlbum
        
        _monitored = State(initialValue: album.monitored)
        _anyReleaseOk = State(initialValue: album.anyReleaseOk)
        _selectedRelease = State(initialValue: album.releases.first(where: { $0.monitored }) ?? album.releases.first)
    }
    
    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Toggle(MR.strings().monitored.localized(), isOn: $monitored)
                    Toggle(MR.strings().automatically_switch_release.localized(), isOn: $anyReleaseOk)
                }
                
                if !album.releases.isEmpty {
                    Section {
                        Picker(MR.strings().releases.localized(), selection: $selectedRelease) {
                            ForEach(album.releases, id: \.id) { release in
                                Text(releaseLabel(for: release))
                                    .tag(Optional(release))
                            }
                        }
                    }
                }
            }
            .navigationTitle(album.title ?? MR.strings().unknown.localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        save()
                    } label: {
                        if editInProgress {
                            ProgressView()
                        } else {
                            Label(MR.strings().save.localized(), systemImage: "checkmark")
                                .foregroundStyle(.white)
                        }
                    }
                    .disabled(editInProgress || (selectedRelease == nil && !album.releases.isEmpty))
                }
            }
        }
    }
    
    private func releaseLabel(for release: AlbumRelease) -> String {
        var parts: [String] = []
        
        if let title = release.title {
            parts.append(title)
        }
        
        parts.append("\(release.mediumCount) \(MR.strings().mediums_short.localized())")
        parts.append("\(release.trackCount) \(MR.strings().tracks_lowercase.localized())")
        
        if !release.country.isEmpty {
            parts.append(release.country.joined(separator: ", "))
        }
        
        var result = parts.joined(separator: ", ")
        
        if let format = release.format {
            result += ", [\(format)]"
        }

        return result
    }
    
    private func save() {
        let updatedReleases = album.releases.map { release in
            release.doCopy(
                id: release.id,
                albumId: release.albumId,
                foreignReleaseId: release.foreignReleaseId,
                title: release.title,
                status: release.status,
                duration: release.duration,
                trackCount: release.trackCount,
                media: release.media,
                mediumCount: release.mediumCount,
                disambiguation: release.disambiguation,
                country: release.country,
                label: release.label,
                format: release.format,
                monitored: release.id == selectedRelease?.id
            )
        }
        
        let updatedAlbum = album.doCopy(
            id: album.id,
            title: album.title,
            overview: album.overview,
            monitored: monitored,
            albumType: album.albumType,
            releaseDate: album.releaseDate,
            genres: album.genres,
            statistics: album.statistics?.doCopy(
                trackFileCount: album.statistics?.trackFileCount ?? 0,
                trackCount: selectedRelease?.trackCount ?? album.statistics?.trackCount ?? 0,
                totalTrackCount: selectedRelease?.trackCount ?? album.statistics?.totalTrackCount ?? 0,
                sizeOnDisk: album.statistics?.sizeOnDisk ?? 0,
                percentOfTracks: album.statistics?.percentOfTracks ?? 0
            ),
            images: album.images,
            artist: album.artist,
            artistId: album.artistId,
            foreignAlbumId: album.foreignAlbumId,
            anyReleaseOk: anyReleaseOk,
            profileId: album.profileId,
            duration: selectedRelease?.duration ?? album.duration,
            ratings: album.ratings,
            releases: updatedReleases,
            instanceId: album.instanceId
        )
        
        onEditAlbum(updatedAlbum)
    }
}
