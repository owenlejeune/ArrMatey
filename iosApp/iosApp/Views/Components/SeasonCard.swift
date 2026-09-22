//
//  SeasonCard.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-15.
//

import SwiftUI
import Shared

struct SeasonCard: View {
    let series: ArrSeries
    var instanceId: Int64? = nil
    let season: Season
    let episodes: [Episode]
    let onToggleSeasonMonitor: (Int32) -> Void
    let onToggleEpisodeMonitor: (Episode) -> Void
    let onEpisodeAutomaticSearch: (Int64) -> Void
    let onSeasonAutomaticSearch: (Int32) -> Void
    let automaticSearchIds: Set<Int64>
    let onDeleteSeason: () -> Void
    let seasonDeleteInProgress: Bool

    @State private var expanded: Bool = false

    @EnvironmentObject private var navigation: NavigationManager

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            seasonHeader

            if expanded {
                seasonDetails

                HStack(spacing: 6) {
                    Button(action: onDeleteSeason) {
                        if seasonDeleteInProgress {
                            ProgressView().progressViewStyle(.circular)
                        } else {
                            Image(systemName: "trash")
                        }
                    }
                    .tint(.red)
                    .buttonStyle(.borderedProminent)
                    .controlSize(.small)

                    ReleaseDownloadButtons(onInteractiveClicked: {
                        if let id = series.id?.int64Value {
                            let route: MediaRoute = .seriesReleases(seriesId: id, seasonNumber: season.seasonNumber, instanceId: instanceId)
                            navigation.go(to: route, of: .sonarr)
                        }
                    }, automaticSearchEnabled: episodes.contains(where: { $0.monitored }), onAutomaticClicked: {
                        onSeasonAutomaticSearch(season.seasonNumber)
                    }, automaticSearchInProgress: false)
                }
                .padding(.bottom, 4)

                ForEach(episodes, id: \.id) { episode in
                    EpisodeRow(episode: episode, instanceId: instanceId, onToggleEpisodeMonitor: { ep in
                        onToggleEpisodeMonitor(ep)
                    }, onAutomaticSearch: {
                        onEpisodeAutomaticSearch(episode.id)
                    }, automaticSearchDisabled: episode.monitored, onClicked: {
                        navigation.go(to: .episodeDetails(series.toJson(), episode.toJson(), instanceId: instanceId), of: .sonarr)
                    })
                    if episode != episodes.last {
                        Divider()
                    }
                }
            }
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .fill(Color(.secondarySystemGroupedBackground))
        )
        .overlay(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(Color.primary.opacity(0.06), lineWidth: 0.5)
        )
        .animation(.spring(response: 0.35, dampingFraction: 0.8), value: expanded)
    }

    private var seasonTitle: String {
        if season.seasonNumber == 0 {
            MR.strings().specials.localized()
        } else {
            MR.strings().season_label.formatted(args: [season.seasonNumber])
        }
    }

    private var episodeStats: String {
        if let stats = season.statistics {
            "\(stats.episodeFileCount)/\(stats.totalEpisodeCount)"
        } else { "" }
    }

    private var year: String {
        episodes.compactMap { $0.airDateUtc }
            .compactMap { instant in
                instant.format(pattern: "yyyy")
            }
            .min()
        ?? String(localized: LocalizedStringResource("tba"))
    }

    private var runtime: String? {
        let items = episodes.compactMap { episode -> Int? in
            episode.runtime.flatMap { runtime in
                runtime.intValue > 0 ? runtime.intValue : nil
            }
        }
        guard !items.isEmpty else { return nil }
        let sorted = items.sorted()
        let median = sorted[sorted.count / 2]
        return median.formatAsRuntime()
    }

    private var seasonInfo: [String] {
        [year, runtime, season.statistics?.sizeOnDisk.bytesAsFileSizeString()]
            .compactMap { $0 }
    }

    private var infoString: String {
        seasonInfo.joined(separator: " • ")
    }

    private var seasonHeader: some View {
        HStack(alignment: .center, spacing: 12) {
            HStack(alignment: .center, spacing: 8) {
                Text(seasonTitle)
                    .font(.headline.weight(.semibold))

                Text(episodeStats)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                Spacer()

                Image(systemName: "chevron.down.circle.fill")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .rotationEffect(.degrees(expanded ? 180 : 0))
                    .animation(.spring(response: 0.35, dampingFraction: 0.8), value: expanded)
            }

            Image(systemName: season.monitored ? "bookmark.fill" : "bookmark")
                .font(.subheadline)
                .foregroundStyle(season.monitored ? Color.accentColor : Color.secondary)
                .onTapGesture {
                    onToggleSeasonMonitor(season.seasonNumber)
                }
        }
        .frame(maxWidth: .infinity)
        .contentShape(Rectangle())
        .onTapGesture {
            expanded = !expanded
        }
    }

    private var seasonDetails: some View {
        VStack(alignment: .leading) {
            Text(infoString)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.leading)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
