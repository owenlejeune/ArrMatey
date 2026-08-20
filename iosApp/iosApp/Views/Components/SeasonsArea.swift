//
//  SeasonsArea.swift
//  iosApp
//
//  A unified seasons component that mirrors Android's SeasonsArea.kt.
//  Takes a List<SeasonWrapper> that combines arr and seerr season data and
//  shows arr controls (monitor, delete, search) only when applicable.
//

import SwiftUI
import Shared

// MARK: - SeasonsArea

struct SeasonsArea: View {
    let seasons: [SeasonWrapper]
    var seriesId: Int64? = nil
    var searchIds: Set<Int64> = []
    var onToggleSeasonMonitor: (Int32) -> Void = { _ in }
    var onToggleEpisodeMonitor: (Episode) -> Void = { _ in }
    var onEpisodeAutomaticSearch: (Int64) -> Void = { _ in }
    var onSeasonAutomaticSearch: (Int32) -> Void = { _ in }
    var deleteSeasonFiles: (Int32) -> Void = { _ in }
    var seasonDeleteInProgress: Bool = false
    var onNavigateToEpisodeDetails: ((Episode) -> Void)? = nil
    var onNavigateToSeriesRelease: ((Int64?, Int32) -> Void)? = nil

    @ViewBuilder
    var body: some View {
        if seasons.isEmpty {
            EmptyView()
        } else {
            VStack(alignment: .leading, spacing: 4) {
                Text(MR.strings().seasons_header.localized())
                    .font(.title3.bold())

                ForEach(seasons, id: \.seasonNumber) { season in
                    SeasonAreaRow(
                        season: season,
                        seriesId: seriesId,
                        searchIds: searchIds,
                        onToggleSeasonMonitor: onToggleSeasonMonitor,
                        onToggleEpisodeMonitor: onToggleEpisodeMonitor,
                        onEpisodeAutomaticSearch: onEpisodeAutomaticSearch,
                        onSeasonAutomaticSearch: onSeasonAutomaticSearch,
                        deleteSeasonFiles: deleteSeasonFiles,
                        seasonDeleteInProgress: seasonDeleteInProgress,
                        onNavigateToEpisodeDetails: onNavigateToEpisodeDetails,
                        onNavigateToSeriesRelease: onNavigateToSeriesRelease
                    )
                }
            }
        }
    }
}

// MARK: - SeasonAreaRow

struct SeasonAreaRow: View {
    let season: SeasonWrapper
    let seriesId: Int64?
    let searchIds: Set<Int64>
    let onToggleSeasonMonitor: (Int32) -> Void
    let onToggleEpisodeMonitor: (Episode) -> Void
    let onEpisodeAutomaticSearch: (Int64) -> Void
    let onSeasonAutomaticSearch: (Int32) -> Void
    let deleteSeasonFiles: (Int32) -> Void
    let seasonDeleteInProgress: Bool
    var onNavigateToEpisodeDetails: ((Episode) -> Void)? = nil
    var onNavigateToSeriesRelease: ((Int64?, Int32) -> Void)? = nil

    @State private var expanded: Bool = false
    @EnvironmentObject private var navigation: NavigationManager

    private var seasonTitle: String {
        season.seasonNumber == 0
            ? MR.strings().specials.localized()
            : MR.strings().season_label.formatted(args: [season.seasonNumber])
    }

    private var statsText: String {
        if let fileCount = season.episodeFileCount {
            "\(fileCount)/\(season.totalEpisodeCount)"
        } else {
            MR.plurals().episodes.localized(Int32(season.totalEpisodeCount))
        }
    }

    /// True when arr season data is present and series has been added to an arr instance.
    private var showArrControls: Bool {
        season.arrSeason != nil && (seriesId ?? 0) > 0
    }

    private var year: String {
        season.episodes.compactMap { $0.airDateUtc }
            .compactMap { $0.format(pattern: "yyyy") }
            .min()
        ?? season.episodes.compactMap { $0.airDate?.year }.min().map { String($0) }
        ?? MR.strings().tba.localized()
    }

    private var runtime: String? {
        let items = season.episodes.compactMap { $0.arrEpisode?.runtime?.intValue }.filter { $0 > 0 }
        guard !items.isEmpty else { return nil }
        let sorted = items.sorted()
        let median = sorted[sorted.count / 2]
        return median.formatAsRuntime()
    }

    private var sizeOnDisk: String? {
        season.arrSeason?.statistics?.sizeOnDisk.bytesAsFileSizeString()
    }

    private var seasonInfoString: String {
        [year, runtime, sizeOnDisk]
            .compactMap { $0 }
            .joined(separator: " • ")
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Tappable header card
            HStack(spacing: 12) {
                Text(seasonTitle)
                    .font(.system(size: 20, weight: .medium))

                Text(statsText)
                    .font(.system(size: 15))
                    .foregroundColor(.secondary)

                Spacer()

                if showArrControls {
                    Button {
                        onToggleSeasonMonitor(season.seasonNumber)
                    } label: {
                        Image(systemName: season.isMonitored ? "bookmark.fill" : "bookmark")
                            .font(.system(size: 17))
                            .foregroundColor(.primary)
                    }
                    .buttonStyle(.plain)
                    .animation(.easeInOut(duration: 0.2), value: season.isMonitored)
                }

                Image(systemName: "chevron.down.circle.fill")
                    .font(.system(size: 20))
                    .rotationEffect(.degrees(expanded ? 180 : 0))
                    .animation(.easeInOut(duration: 0.25), value: expanded)
            }
            .padding(.vertical, 12)
            .padding(.horizontal, 16)
            .background(Color(.secondarySystemBackground))
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            .contentShape(Rectangle())
            .onTapGesture {
                withAnimation(.easeInOut(duration: 0.25)) {
                    expanded.toggle()
                }
            }
            .padding(.vertical, 4)

            // Expanded content
            if expanded {
                VStack(alignment: .leading, spacing: 10) {
                    if !seasonInfoString.isEmpty {
                        Text(seasonInfoString)
                            .font(.system(size: 16))
                            .foregroundColor(.primary)
                            .padding(.top, 4)
                            .padding(.horizontal, 4)
                    }

                    if showArrControls, let sId = seriesId {
                        ReleaseDownloadButtons(
                            onInteractiveClicked: {
                                if let onNavigateToSeriesRelease = onNavigateToSeriesRelease {
                                    onNavigateToSeriesRelease(sId, season.seasonNumber)
                                } else {
                                    navigation.go(to: .seriesReleases(seriesId: sId, seasonNumber: season.seasonNumber, episodeId: nil), of: .sonarr)
                                }
                            },
                            automaticSearchEnabled: season.episodes.contains { $0.isMonitored },
                            onAutomaticClicked: {
                                onSeasonAutomaticSearch(season.seasonNumber)
                            },
                            automaticSearchInProgress: searchIds.contains(Int64(season.seasonNumber)) || searchIds.contains(sId),
                            deleteInProgress: seasonDeleteInProgress,
                            onDelete: {
                                deleteSeasonFiles(season.seasonNumber)
                            }
                        )
                        .padding(.horizontal, 4)
                        .padding(.top, 2)
                    }

                    let episodes = season.episodes
                    ForEach(Array(episodes.enumerated()), id: \.offset) { index, wrapper in
                        EpisodeRow(
                            episode: wrapper,
                            searchInProgress: { searchIds.contains($0) },
                            onAutomaticSearch: { onEpisodeAutomaticSearch($0) },
                            onToggleMonitor: { onToggleEpisodeMonitor($0) },
                            onNavigateToSeriesRelease: { epId in
                                if let onNavigateToSeriesRelease = onNavigateToSeriesRelease {
                                    onNavigateToSeriesRelease(seriesId, season.seasonNumber)
                                } else {
                                    navigation.go(to: .seriesReleases(seriesId: seriesId, seasonNumber: nil, episodeId: epId), of: .sonarr)
                                }
                            },
                            onClick: {
                                if let arrEp = wrapper.arrEpisode {
                                    onNavigateToEpisodeDetails?(arrEp)
                                }
                            }
                        )
                        .padding(.horizontal, 4)

                        if index < episodes.count - 1 {
                            Divider().padding(.horizontal, 4)
                        }
                    }
                }
                .padding(.leading, 8)
                .padding(.bottom, 8)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
    }
}
