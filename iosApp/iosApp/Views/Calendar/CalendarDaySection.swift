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
    let movies: [ArrMovie]
    let episodeGroups: [EpisodeGroup]
    let albums: [ArrAlbum]
    let isToday: Bool
    
    private var totalEpisodes: Int {
        episodeGroups.reduce(0) { $0 + Int($1.totalCount) }
    }
    
    private var totalItems: Int {
        movies.count + totalEpisodes
    }
    
    private var dateString: String {
        var components = DateComponents()
        components.year = Int(date.year)
        components.month = Int(date.month.number())
        components.day = Int(date.dayOfMonth)
        
        if let swiftDate = Calendar.current.date(from: components) {
            return swiftDate.relativeString()
        }
        
        return date.dayOfWeek.name.capitalized
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
            
            ForEach(movies, id: \.id) { movie in
                MovieCalendarItem(movie: movie)
            }
            
            ForEach(episodeGroups.indices, id: \.self) { index in
                EpisodeCalendarItem(episodeGroup: episodeGroups[index])
            }
            
            ForEach(albums, id: \.self) { album in
                AlbumCalendarItem(album: album)
            }
        }
    }
}
