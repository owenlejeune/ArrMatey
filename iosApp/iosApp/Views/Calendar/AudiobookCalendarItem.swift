//
//  AudiobookCalendarItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-19.
//

import SwiftUI
import Shared

struct AudiobookCalendarItem: View {
    let audiobook: Audiobook
    let instances: [Instance]
    var useFullColorCards: Bool = false
    let onNavigate: (Int64?) -> Void
    
    private var associatedColor: Color {
        .arrLightPurple
    }
    
    private var statusIcon: String? {
        if audiobook.isDownloaded {
            return "square.and.arrow.down.fill"
        } else if audiobook.monitored {
            return "bookmark.fill"
        } else if !audiobook.monitored {
            return "bookmark"
        }
        return nil
    }
    
    var statusText: String {
        let authorsString = audiobook.authors.joined(separator: ", ")
        let series = audiobook.series
        return [authorsString, series]
            .compactMap { $0 }
            .joined(separator: " • ")
    }
    
    var body: some View {
        SlidableCalendarItem(
            instanceIds: audiobook.instanceIds,
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
                    GenericPosterItem(posterUrl: audiobook.getPoster()?.remoteUrl, aspectRatio: .cover)
                        .frame(width: 50)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(audiobook.title ?? "")
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
