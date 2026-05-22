//
//  CalendarDayCell.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import SwiftUI
import Shared

struct CalendarDayCell: View {
    let date: LocalDate
    let isSelected: Bool
    let items: [CalendarItem]
    let onClick: () -> Void
    
    private var isToday: Bool {
        let today = Calendar.current.dateComponents([.year, .month, .day], from: Date())
        return Int(date.year) == today.year &&
                Int(date.month.number()) == today.month &&
                Int(date.day) == today.day
    }
    
    var body: some View {
        Button(action: onClick) {
            VStack(spacing: 4) {
                Text("\(date.day)")
                    .font(.body)
                    .fontWeight(isToday || isSelected ? .bold : .regular)
                    .foregroundColor(isSelected ? .white : (isToday ? .themePrimary : .primary))
                
                Spacer()
                
                if items.count > 0 {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 2) {
                        let movieCount = items.count(where: { $0 is ArrMovie })
                        if movieCount > 0 {
                            CountBadge(count: movieCount, color: .arrOrange)
                        }
                        let episodeCount = items.count(where: { $0 is Episode })
                        if episodeCount > 0 {
                            CountBadge(count: episodeCount, color: .arrBlue)
                        }
                        let albumCount = items.count(where: { $0 is ArrAlbum })
                        if albumCount > 0 {
                            CountBadge(count: albumCount, color: .arrGreen)
                        }
                        let bookCount = items.count(where: { $0 is Book })
                        if bookCount > 0 {
                            CountBadge(count: bookCount, color: .arrRed)
                        }
                        let audiobookCount = items.count(where: { $0 is Audiobook })
                        if audiobookCount > 0 {
                            CountBadge(count: audiobookCount, color: .arrLightPurple)
                        }
                    }
                } else {
                    Spacer()
                        .frame(height: 16)
                }
            }
            .frame(maxWidth: .infinity)
            .aspectRatio(1, contentMode: .fit)
            .padding(4)
            .background(backgroundColor)
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(isToday && !isSelected ? Color.themePrimary : Color.clear, lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
    }
    
    private var backgroundColor: Color {
        if isSelected {
            return .themePrimary
        } else if isToday {
            return .themePrimary.opacity(0.15)
        } else {
            return .clear
        }
    }
}

struct CountBadge: View {
    let count: Int
    let color: Color
    
    var body: some View {
        Text(count > 9 ? "9+" : "\(count)")
            .font(.system(size: 8))
            .fontWeight(.bold)
            .foregroundColor(.white)
            .frame(width: 16, height: 16)
            .background(color)
            .clipShape(Circle())
    }
}
