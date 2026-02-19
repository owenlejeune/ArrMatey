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
        
        let timeInterval = TimeInterval(airDateUtc.epochSeconds)
        let date = Date(timeIntervalSince1970: timeInterval)
        
        let formatter = DateFormatter()
        formatter.dateStyle = .none
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
    
    var body: some View {
        HStack(spacing: 12) {
            if let series = episode.series {
                PosterItem(item: series, radius: 8)
                    .frame(width: 45)
            }
            
            VStack(alignment: .leading, spacing: 2) {
                Text(episode.series?.title ?? MR.strings().unknown.localized())
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.primary)
                    .lineLimit(1)
                
                Text("S\(episode.seasonNumber)E\(episode.episodeNumber) • \(episode.title ?? "")")
                    .font(.system(size: 14))
                    .foregroundColor(.secondary)
                    .lineLimit(1)
                
                HStack(spacing: 6) {
                    if let airTime = airTime {
                        Label(airTime, systemImage: "clock")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(.secondary)
                    }
                    
                    if isPremier {
                        BadgeView(text: MR.strings().premier.localized(), color: .orange)
                    }
                    
                    if let finaleType = episode.finaleType {
                        BadgeView(text: finaleType.resource.localized(), color: .purple)
                    }
                    
                    if !episodeGroup.additional.isEmpty {
                        Text("+\(episodeGroup.additional.count)")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.blue)
                    }
                }
                .padding(.top, 2)
            }
            
            Spacer()
            
            if let icon = statusIcon {
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundColor(episode.hasFile ? .green : .secondary)
            }
        }
        .padding(10)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(12)
    }
}
