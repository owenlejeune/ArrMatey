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
    let onNavigate: (Int64?) -> Void
    
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
            HStack(spacing: 12) {
                GenericPosterItem(posterUrl: book.getCover()?.remoteUrl, aspectRatio: .poster)
                    .frame(width: 50)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(book.title)
                        .font(.headline)
                        .foregroundColor(.black)
                    
                    Text(statusText)
                        .font(.subheadline)
                        .foregroundColor(.black)
                }
                
                Spacer()
                
                if let icon = statusIcon {
                    Image(systemName: icon)
                        .font(.system(size: 18))
                        .foregroundColor(.black)
                }
            }
            .padding()
            .background(.arrRed)
            .cornerRadius(12)
        }
    }
}
