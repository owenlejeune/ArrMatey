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
    let onNavigate: (Int64?) -> Void
    
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
        HStack(spacing: 12) {
            PosterItem(item: movie)
                .frame(width: 50)
            
            VStack(alignment: .leading, spacing: 6) {
                Text(movie.title ?? MR.strings().unknown.localized())
                    .font(.headline)
                    .foregroundColor(.black)
                
                if let releaseType = releaseTypeText {
                    HStack(spacing: 8) {
                        Text(releaseType)
                            .font(.footnote)
                            .foregroundColor(.black)
                    }
                }
                
                if !infoString.isEmpty {
                    Text(infoString)
                        .font(.footnote)
                        .foregroundColor(.black)
                }
            }
            
            Spacer()
            
            if let icon = statusIcon {
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(.black)
            }
        }
        .padding()
        .background(.arrOrange)
        .cornerRadius(12)
        .onTapGesture {
            if movie.instanceIds.count > 1 {
                // Handled by swipe actions
            } else {
                onNavigate(movie.instanceIds.first?.int64Value)
            }
        }
        .swipeActions(edge: .trailing) {
            if movie.instanceIds.count > 1 {
                ForEach(instances.filter { movie.instanceIds.contains(Int64(truncating: $0.id as NSNumber)) }, id: \.id) { instance in
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
