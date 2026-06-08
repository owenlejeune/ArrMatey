//
//  EpisodeCalendarItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-09.
//

import SwiftUI
import Shared

struct EpisodeCalendarItem: View {
    let episodeGroup: EpisodeGroup
    
    private var episode: Episode {
        episodeGroup.first
    }
    
    private var isPremier: Bool {
        episode.seasonNumber == 1 && episode.episodeNumber == 1
    }
    
    private var statusIcon: String? {
        if episode.hasFile {
            return "checkmark.circle.fill"
        } else if !episode.monitored {
            return "bookmark"
        } else if !episode.hasAired {
            return "clock.fill"
        } else if episode.monitored {
            return "bookmark.fill"
        } else if !episode.monitored {
            return "bookmark"
        }
        return nil
    }
    
    private var airTime: String? {
        guard let airDateUtc = episode.airDateUtc else { return nil }
        
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        formatter.timeZone = TimeZone.current
        
        let timeInterval = TimeInterval(airDateUtc.epochSeconds)
        let date = Date(timeIntervalSince1970: timeInterval)
        
        return formatter.string(from: date)
    }
    
    var body: some View {
        HStack(spacing: 12) {
            if let series = episode.series {
                PosterItem(item: series)
                    .frame(width: 50)
            }
            
            VStack(alignment: .leading, spacing: 6) {
                Text(episode.series?.title ?? MR.strings().unknown.localized())
                    .font(.headline)
                    .foregroundColor(.black)
                
                Text("S\(episode.seasonNumber)E\(episode.episodeNumber) • \(episode.title ?? "")")
                    .font(.subheadline)
                
                HStack(spacing: 8) {
                    if let airTime = airTime {
                        Text(airTime)
                            .font(.footnote)
                            .foregroundColor(.black)
                    }
                    
                    if isPremier {
                        BadgeView(text: MR.strings().premier.localized(), color: .arrGrey)
                    }
                    
                    if let finaleType = episode.finaleType {
                        BadgeView(text: finaleType.resource.localized(), color: .arrGrey)
                    }
                    
                    if !episodeGroup.additional.isEmpty {
                        Text(MR.strings().additional_items_count.formatted(args: [episodeGroup.additional.count]))
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
            }
            
            Spacer()
            
            if let icon = statusIcon {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundColor(.black)
            }
        }
        .padding()
        .background(.arrBlue)
        .cornerRadius(12)
    }
}
