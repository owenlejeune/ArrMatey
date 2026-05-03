//
//  MediaInfoArea.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-15.
//

import SwiftUI
import Shared

struct MediaInfoArea: View {
    let item: ArrMedia
    let qualityProfiles: [QualityProfile]
    let tags: [Tag]
    
    private var infoItems: [InfoItem] {
        if let series = item as? ArrSeries {
            seriesInfo(series)
        } else if let movie = item as? ArrMovie {
            movieInfo(movie)
        } else if let artist = item as? Arrtist {
            artistInfo(artist)
        } else if let author = item as? Author {
            authorInfo(author)
        } else { [] }
    }
    
    var body: some View {
        Section {
            VStack(spacing: 12) {
                ForEach(Array(infoItems), id: \.self) { info in
                    HStack(alignment: .center) {
                        Text(info.label)
                            .font(.system(size: 14))
                        Spacer()
                        Text(info.value)
                            .font(.system(size: 14))
                            .foregroundColor(.themePrimary)
                            .lineLimit(1)
                            .truncationMode(.tail)
                            .multilineTextAlignment(.trailing)
                            .frame(maxWidth: .infinity, alignment: .trailing)
                    }
                    
                    if info != infoItems.last {
                        Divider()
                    }
                }
            }
        } header: {
            Text(MR.strings().information.localized())
                .font(.system(size: 26, weight: .bold))
        }
    }
    
    private func seriesInfo(_ series: ArrSeries) -> [InfoItem] {
        let qualityProfile = qualityProfiles.first(where: { $0.id == series.qualityProfileId })
        let qualityLabel = qualityProfile?.name ?? MR.strings().unknown.localized()
        let tagsLabel = series.formatTags(availableTags: tags) ?? MR.strings().none.localized()
        
        let unknown = MR.strings().unknown.localized()
        
        let monitorLabel = if series.monitorNewItems == .all {
            MR.strings().monitored.localized()
        } else {
            MR.strings().unmonitored.localized()
        }
        
        let seasonFolderLabel = if series.seasonFolder {
            MR.strings().yes.localized()
        } else {
            MR.strings().no.localized()
        }
        
        let diskSize = series.fileSize.bytesAsFileSizeString()
        
        return [
            InfoItem(label: MR.strings().series_type.localized(), value: series.seriesType.name),
            InfoItem(label: MR.strings().size_on_disk.localized(), value: diskSize),
            InfoItem(label: MR.strings().root_folder.localized(), value: series.rootFolderPath ?? unknown),
            InfoItem(label: MR.strings().path.localized(), value: series.path ?? unknown),
            InfoItem(label: MR.strings().new_seasons.localized(), value: monitorLabel),
            InfoItem(label: MR.strings().season_folders.localized(), value: seasonFolderLabel),
            InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel),
            InfoItem(label: MR.strings().tags.localized(), value: tagsLabel)
        ]
    }

    private func movieInfo(_ movie: ArrMovie) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        
        let qualityProfile = qualityProfiles.first(where: { $0.id == movie.qualityProfileId })
        let qualityLabel = qualityProfile?.name ?? unknown
        let tagsLabel = movie.formatTags(availableTags: tags) ?? MR.strings().none.localized()
        
        let rootFolderValue = movie.rootFolderPath.isEmpty ? unknown : movie.rootFolderPath
        
        var info: [InfoItem] = [
            InfoItem(label: MR.strings().minimum_availability.localized(), value: movie.minimumAvailability.name),
            InfoItem(label: MR.strings().root_folder.localized(), value: rootFolderValue),
            InfoItem(label: MR.strings().path.localized(), value: movie.path ?? unknown)
        ]
        
        if let inCinemas = movie.inCinemas?.format(pattern: "MMM d, yyyy") {
            info.append(InfoItem(label: MR.strings().in_cinemas.localized(), value: inCinemas))
        }
        
        if let physicalRelease = movie.physicalRelease?.format(pattern: "MMM d, yyyy") {
            info.append(InfoItem(label: MR.strings().physical_release.localized(), value: physicalRelease))
        }
        
        if let digitalRelease = movie.digitalRelease?.format(pattern: "MMM d, yyyy") {
            info.append(InfoItem(label: MR.strings().digital_release.localized(), value: digitalRelease))
        }
        
        info.append(InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel))
        info.append(InfoItem(label: MR.strings().tags.localized(), value: tagsLabel))
        
        return info
    }

    private func artistInfo(_ artist: Arrtist) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        
        let qualityProfile = qualityProfiles.first(where: { $0.id == artist.qualityProfileId })
        let qualityLabel = qualityProfile?.name ?? unknown
        let tagsLabel = artist.formatTags(availableTags: tags) ?? MR.strings().none.localized()
        
        let monitorLabel = if artist.monitorNewItems == .all {
            MR.strings().monitored.localized()
        } else {
            MR.strings().unmonitored.localized()
        }
        
        let rootFolderValue = if let path = artist.rootFolderPath, !path.isEmpty {
            path
        } else {
            unknown
        }
        
        let diskSize = artist.fileSize.bytesAsFileSizeString()
        
        return [
            InfoItem(label: MR.strings().size_on_disk.localized(), value: diskSize),
            InfoItem(label: MR.strings().root_folder.localized(), value: rootFolderValue),
            InfoItem(label: MR.strings().path.localized(), value: artist.path ?? unknown),
            InfoItem(label: MR.strings().new_albums.localized(), value: monitorLabel),
            InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel),
            InfoItem(label: MR.strings().tags.localized(), value: tagsLabel)
        ]
    }
    
    private func authorInfo(_ author: Author) -> [InfoItem] {
        let unknown = MR.strings().unknown.localized()
        
        let qualityProfile = qualityProfiles.first(where: { $0.id == author.qualityProfileId })
        let qualityLabel = qualityProfile?.name ?? unknown
        let tagsLabel = author.formatTags(availableTags: tags) ?? MR.strings().none.localized()
        
        let monitorLabel = if author.monitorNewItems == .all {
            MR.strings().monitored.localized()
        } else {
            MR.strings().unmonitored.localized()
        }
        
        let rootFolderValue = if let path = author.rootFolderPath, !path.isEmpty {
            path
        } else {
            unknown
        }
        
        let diskSize = author.fileSize.bytesAsFileSizeString()
        
        return [
            InfoItem(label: MR.strings().size_on_disk.localized(), value: diskSize),
            InfoItem(label: MR.strings().root_folder.localized(), value: rootFolderValue),
            InfoItem(label: MR.strings().path.localized(), value: author.path ?? unknown),
            InfoItem(label: MR.strings().new_albums.localized(), value: monitorLabel),
            InfoItem(label: MR.strings().quality_profile.localized(), value: qualityLabel),
            InfoItem(label: MR.strings().tags.localized(), value: tagsLabel)
        ]
    }
}
