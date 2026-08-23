//
//  CalendarDaySection.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import Shared
import SwiftUI

struct CalendarDaySection: View {
    let date: LocalDate
    let items: [CalendarItem]
    let isToday: Bool
    let instances: [Instance]
    let navigationManager: NavigationManager
    
    private var totalItems: Int {
        items.count(where: { !($0 is EpisodeGroup) })
    }
    
    private var dateString: String {
        let dayOfWeek = date.dayOfWeek.name.capitalized
        return isToday ? MR.strings().today.localized() : dayOfWeek
    }
    
    private var dateDetailString: String {
        let monthAbbr = date.month.name.prefix(3).capitalized
        return "\(monthAbbr) \(date.day), \(date.year)"
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(dateString)
                        .font(isToday ? .title2.bold() : .title2)
                        .foregroundColor(isToday ? .themePrimary : .primary)
                    
                    Text(dateDetailString)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                if totalItems > 0 {
                    Text("\(totalItems)")
                        .font(.caption.bold())
                        .foregroundColor(.white)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(.themePrimary)
                        .clipShape(Capsule())
                }
            }
            
            ForEach(items, id: \.calendarId) { item in
                let onNavigate: (Int64?) -> Void = { instanceId in
                    let instanceType = (item as? InstanceTypeIdentifiable)?.instanceType ?? .sonarr
                    switch item {
                    case let movie as ArrMovie:
                        navigationManager.go(to: .details(arrId: movie.id?.int64Value, tmdbId: movie.tmdbId, instanceType: instanceType), of: instanceType)
                    case let epGroup as EpisodeGroup:
                        navigationManager.go(to: .details(arrId: epGroup.first.seriesId, instanceType: instanceType), of: instanceType)
                    case let episode as Episode:
                        if let series = episode.series {
                            navigationManager.go(to: .details(arrId: series.id?.int64Value, tmdbId: series.tmdbId?.int64Value, instanceType: instanceType), of: instanceType)
                            navigationManager.go(to: .episodeDetails(series.toJson(), episode.toJson()), of: instanceType)
                        }
                    case let album as ArrAlbum:
                        navigationManager.go(to: .details(arrId: album.id, instanceType: instanceType), of: instanceType)
                    case let book as Book:
                        if let author = book.author {
                            navigationManager.go(to: .details(arrId: author.id?.int64Value, instanceType: instanceType), of: instanceType)
                            navigationManager.go(to: .bookDetails(bookJson: book.toJson(), authorJson: author.toJson()), of: instanceType)
                        }
                    case let audiobook as Audiobook:
                        navigationManager.go(to: .details(arrId: audiobook.id, instanceType: instanceType), of: instanceType)
                    default: break
                    }
                }

                switch item {
                case let movie as ArrMovie: MovieCalendarItem(movie: movie, date: date, instances: instances, onNavigate: onNavigate)
                case let epGroup as EpisodeGroup: EpisodeCalendarItem(episode: epGroup.first, additional: epGroup.additional, instances: instances, onNavigate: onNavigate)
                case let episode as Episode: EpisodeCalendarItem(episode: episode, instances: instances, onNavigate: onNavigate)
                case let album as ArrAlbum: AlbumCalendarItem(album: album, instances: instances, onNavigate: onNavigate)
                case let book as Book: BookCalendarItem(book: book, instances: instances, onNavigate: onNavigate)
                case let audiobook as Audiobook: AudiobookCalendarItem(audiobook: audiobook, instances: instances, onNavigate: onNavigate)
                default: EmptyView()
                }
            }
        }
    }
}
