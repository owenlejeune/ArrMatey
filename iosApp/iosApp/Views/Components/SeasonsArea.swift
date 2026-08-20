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
                        seasonDeleteInProgress: seasonDeleteInProgress
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
                VStack(alignment: .leading, spacing: 0) {
                    if showArrControls {
                        seasonActions
                            .padding(.horizontal, 4)
                            .padding(.top, 8)
                    }

                    let episodes = season.episodes
                    ForEach(Array(episodes.enumerated()), id: \.offset) { index, wrapper in
                        episodeView(wrapper)
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

    @ViewBuilder
    private var seasonActions: some View {
        HStack(spacing: 6) {
            Button {
                deleteSeasonFiles(season.seasonNumber)
            } label: {
                if seasonDeleteInProgress {
                    ProgressView().controlSize(.small)
                } else {
                    Image(systemName: "trash")
                }
            }
            .tint(.red)
            .buttonStyle(.borderedProminent)
            .controlSize(.small)

            let hasMonitoredEps = season.episodes.contains { $0.isMonitored }
            Button {
                onSeasonAutomaticSearch(season.seasonNumber)
            } label: {
                Image(systemName: "magnifyingglass")
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.small)
            .disabled(!hasMonitoredEps)

            Button {
                if let id = seriesId {
                    navigation.go(to: .seriesReleases(seriesId: id, seasonNumber: season.seasonNumber), of: .sonarr)
                }
            } label: {
                Image(systemName: "person.fill")
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.small)
        }
        .padding(.bottom, 8)
    }

    @ViewBuilder
    private func episodeView(_ wrapper: EpisodeWrapper) -> some View {
        if let arrEp = wrapper.arrEpisode {
            EpisodeRow(
                episode: arrEp,
                onToggleEpisodeMonitor: { onToggleEpisodeMonitor($0) },
                onAutomaticSearch: { onEpisodeAutomaticSearch(arrEp.id) },
                automaticSearchDisabled: arrEp.monitored == false,
                onClicked: {}
            )
        } else {
            SeerrEpisodeWrapperRow(wrapper: wrapper)
        }
    }
}

// MARK: - Seerr-only episode row (uses EpisodeWrapper fields)

struct SeerrEpisodeWrapperRow: View {
    let wrapper: EpisodeWrapper

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(alignment: .top) {
                Text("\(wrapper.episodeNumber) - \(wrapper.title ?? "")")
                    .font(.subheadline.bold())
                    .foregroundColor(.accentColor)

                Spacer()

                if let airDate = wrapper.airDate {
                    Text(airDate.format(pattern: "MMM d, yyyy"))
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }

            if let overview = wrapper.overview, !overview.isEmpty {
                Text(overview)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(3)
            }

            if let stillPath = wrapper.stillPath,
               let url = URL(string: "https://image.tmdb.org/t/p/w500\(stillPath)") {
                AsyncImage(url: url) { image in
                    image.resizable().aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color(.systemGray5)
                }
                .frame(height: 120)
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 4)
    }
}
