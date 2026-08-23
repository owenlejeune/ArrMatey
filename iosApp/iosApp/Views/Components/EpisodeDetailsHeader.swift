//
//  EpisodeDetailsHeader.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-24.
//

import Shared
import SwiftUI

struct EpisodeDetailsHeader: View {
    let series: ArrSeries
    let episode: Episode
    
    var body: some View {
        ZStack(alignment: .bottomLeading) {
            MediaHeaderBanner(bannerUrl: URL(string: episode.getBanner()?.remoteUrl ?? ""), height: 280)
            
            LinearGradient(
                gradient: Gradient(colors: [
                    .clear,
                    Color(.systemBackground).opacity(0.8),
                    Color(.systemBackground)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .frame(height: 120)

            HStack(alignment: .bottom, spacing: 18) {
                if let url = episode.getPoster()?.remoteUrl {
                    AsyncImage(url: URL(string: url)) { image in
                        image.image?
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    }
                    .frame(width: 140, height: 80)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .shadow(radius: 4)
                }
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(series.title ?? MR.strings().unknown.localized())
                        .font(.system(size: 18, weight: .semibold))
                    
                    Text(statusRow)
                        .font(.system(size: 14))
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 12)
        }
    }
    
    private var statusRow: String {
        [
            episode.seasonEpLabel,
            episode.runtimeString,
            episode.formatAirDateUtc()
        ]
        .compactMap { $0 }
        .joined(separator: " • ")
    }
}
