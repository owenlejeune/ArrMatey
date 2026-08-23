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
                switch item {
                case let movie as ArrMovie: MovieCalendarItem(movie: movie, date: date)
                case let epGroup as EpisodeGroup: EpisodeCalendarItem(episodeGroup: epGroup)
                case let album as ArrAlbum: AlbumCalendarItem(album: album)
                case let book as Book: BookCalendarItem(book: book)
                case let audiobook as Audiobook: AudiobookCalendarItem(audiobook: audiobook)
                default: EmptyView()
                }
            }
        }
    }
}
