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
    let onNavigate: (Int64?) -> Void
    
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
        HStack(spacing: 12) {
            AlbumCoverView(album: album)
                .frame(width: 50, height: 50)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(album.title ?? MR.strings().unknown.localized())
                    .font(.headline)
                    .foregroundColor(.white)
                
                Text(album.artist?.title ?? MR.strings().unknown.localized())
                    .font(.subheadline)
                    .foregroundColor(.white)
            }
            
            Spacer()
            
            if let icon = statusIcon {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(.white)
            }
        }
        .padding()
        .background(.arrGreen)
        .cornerRadius(12)
        .onTapGesture {
            if album.instanceIds.count > 1 {
                // Handled by swipe actions
            } else {
                onNavigate(album.instanceIds.first?.int64Value)
            }
        }
        .swipeActions(edge: .trailing) {
            if album.instanceIds.count > 1 {
                ForEach(instances.filter { album.instanceIds.contains(Int64(truncating: $0.id as NSNumber)) }, id: \.id) { instance in
                    Button {
                        onNavigate(instance.id)
                    } label: {
                        Text(instance.label)
                    }
                }
            }
        }
    }
}
