//
//  AlbumCalendarItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-13.
//

import SwiftUI
import Shared

struct AlbumCalendarItem: View {
    let album: ArrAlbum
    let instances: [Instance]
    var useFullColorCards: Bool = false
    let onNavigate: (Int64?) -> Void
    
    private var associatedColor: Color {
        .arrGreen
    }
    
    private var statusIcon: String? {
        if album.isDownloaded {
            return "square.and.arrow.down.fill"
        } else if album.isPartiallyDownloaded {
            return "arrow.down.circle.dotted"
        } else if album.monitored {
            return "bookmark.fill"
        } else if !album.monitored {
            return "bookmark"
        }
        return nil
    }
    
    var body: some View {
        SlidableCalendarItem(
            instanceIds: album.instanceIds,
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
                    AlbumCoverView(album: album)
                        .frame(width: 50, height: 50)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text(album.title ?? MR.strings().unknown.localized())
                            .font(.headline)
                            .foregroundColor(useFullColorCards ? .white : .primary)
                        
                        Text(album.artist?.title ?? MR.strings().unknown.localized())
                            .font(.subheadline)
                            .foregroundColor(useFullColorCards ? .white.opacity(0.85) : .secondary)
                    }
                    
                    Spacer()
                    
                    if let icon = statusIcon {
                        Image(systemName: icon)
                            .font(.system(size: 18))
                            .foregroundColor(useFullColorCards ? .white : .secondary)
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
