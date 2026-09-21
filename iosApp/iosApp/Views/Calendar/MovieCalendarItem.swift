//
//  MovieCalendarItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import SwiftUI
import Shared

struct MovieCalendarItem: View {
    let movie: ArrMovie
    let date: LocalDate
    let instances: [Instance]
    var useFullColorCards: Bool = false
    let onNavigate: (Int64?) -> Void
    
    private var associatedColor: Color {
        .arrOrange
    }
    
    private var statusIcon: String? {
        if movie.isDownloaded {
            return "checkmark.circle.fill"
        } else if !movie.monitored {
            return "bookmark"
        } else if movie.isWaiting {
            return "clock.fill"
        } else if movie.monitored {
            return "bookmark.fill"
        }
        return nil
    }
    
    private var releaseTypeText: String? {
        if movie.inCinemas?.isEqual(date: date) == true {
            MR.strings().in_cinemas.localized()
        } else if movie.digitalRelease?.isEqual(date: date) == true {
            MR.strings().digital_release.localized()
        } else if movie.physicalRelease?.isEqual(date: date) == true {
            MR.strings().physical_release.localized()
        } else {
            nil
        }
    }
    
    private var infoString: String {
        [movie.certification, movie.studio]
            .compactMap{ $0 }
            .joined(separator: " • ")
    }
    
    var body: some View {
        SlidableCalendarItem(
            instanceIds: movie.instanceIds,
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
                    PosterItem(item: movie)
                        .frame(width: 50)
                    
                    VStack(alignment: .leading, spacing: 6) {
                        Text(movie.title ?? MR.strings().unknown.localized())
                            .font(.headline)
                            .foregroundColor(useFullColorCards ? .black : .primary)
                        
                        if let releaseType = releaseTypeText {
                            HStack(spacing: 8) {
                                Text(releaseType)
                                    .font(.footnote)
                                    .foregroundColor(useFullColorCards ? .black.opacity(0.85) : .secondary)
                            }
                        }
                        
                        if !infoString.isEmpty {
                            Text(infoString)
                                .font(.footnote)
                                .foregroundColor(useFullColorCards ? .black.opacity(0.75) : .secondary)
                        }
                    }
                    
                    Spacer()
                    
                    if let icon = statusIcon {
                        Image(systemName: icon)
                            .font(.system(size: 20))
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
