//
//  BookCalendarItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-07.
//

import SwiftUI
import Shared

struct BookCalendarItem: View {
    let book: Book
    let instances: [Instance]
    var useFullColorCards: Bool = false
    let onNavigate: (Int64?) -> Void
    
    private var associatedColor: Color {
        .arrRed
    }
    
    private var statusIcon: String? {
        if book.isDownloaded {
            return "square.and.arrow.down.fill"
        } else if book.isPartiallyDownloaded {
            return "arrow.down.circle.dotted"
        } else if book.monitored {
            return "bookmark.fill"
        } else if !book.monitored {
            return "bookmark"
        }
        return nil
    }
    
    var statusText: String {
        let seriesTitle = book.seriesTitle?.isEmpty == true ? nil : book.seriesTitle
        return [book.authorTitle, seriesTitle]
            .compactMap { $0 }
            .joined(separator: " • ")
    }
    
    var body: some View {
        SlidableCalendarItem(
            instanceIds: book.instanceIds,
            instances: instances,
            onInstanceSelected: onNavigate
        ) {
            HStack(spacing: 0) {
                if !useFullColorCards {
                    Rectangle()
                        .fill(associatedColor)
                        .frame(width: 5)
                }

                HStack(spacing: 12) {
                    GenericPosterItem(posterUrl: book.getCover()?.remoteUrl, aspectRatio: .poster)
                        .frame(width: 50)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(book.title)
                            .font(.headline)
                            .foregroundColor(useFullColorCards ? .black : .primary)
                        
                        Text(statusText)
                            .font(.subheadline)
                            .foregroundColor(useFullColorCards ? .black.opacity(0.85) : .secondary)
                    }
                    
                    Spacer()
                    
                    if let icon = statusIcon {
                        Image(systemName: icon)
                            .font(.system(size: 18))
                            .foregroundColor(useFullColorCards ? .black : .secondary)
                    }
                }
                .padding(12)
            }
            .background(useFullColorCards ? associatedColor : Color(uiColor: .secondarySystemGroupedBackground))
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(useFullColorCards ? Color.clear : Color.primary.opacity(0.06), lineWidth: 0.5)
            )
        }
    }
}
