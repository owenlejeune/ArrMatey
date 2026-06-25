//
//  BazarrTab.swift
//  iosApp
//

import SwiftUI
import Shared

struct BazarrTab: View {
    @Environment(\.navigationContext) private var context

    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack {
                BazarrTabContent()
            }
        case .launcher:
            BazarrTabContent()
        }
    }
}

private struct SearchTarget: Identifiable {
    let id = UUID()
    let target: BazarrMediaTarget
}

struct BazarrTabContent: View {
    @State private var segment = 0
    @State private var searchTarget: SearchTarget?
    @ObservedObject private var viewModel = BazarrViewModelS()

    var body: some View {
        VStack(spacing: 0) {
            Picker("", selection: $segment) {
                Text(MR.strings().bazarr_wanted_episodes.localized()).tag(0)
                Text(MR.strings().bazarr_wanted_movies.localized()).tag(1)
                Text(MR.strings().bazarr_providers.localized()).tag(2)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)

            switch segment {
            case 0:
                WantedEpisodesList(
                    state: viewModel.wantedEpisodesState,
                    onLoadMore: viewModel.loadMoreEpisodes,
                    onRefresh: viewModel.refresh
                ) { episode in
                    searchTarget = SearchTarget(
                        target: BazarrMediaTargetEpisode(
                            seriesId: episode.sonarrSeriesId,
                            episodeId: episode.sonarrEpisodeId
                        )
                    )
                }
            case 1:
                WantedMoviesList(
                    state: viewModel.wantedMoviesState,
                    onLoadMore: viewModel.loadMoreMovies,
                    onRefresh: viewModel.refresh
                ) { movie in
                    searchTarget = SearchTarget(
                        target: BazarrMediaTargetMovie(radarrId: movie.radarrId)
                    )
                }
            default:
                ProvidersList(
                    state: viewModel.providersState,
                    onRefresh: viewModel.loadProviders,
                    onReset: viewModel.resetProviders
                )
            }
        }
        .navigationTitle(MR.strings().bazarr.localized())
        .sheet(item: $searchTarget) { item in
            BazarrSubtitleSearchSheet(target: item.target)
        }
    }
}

private struct WantedEpisodesList: View {
    let state: PagedData<WantedEpisode>
    let onLoadMore: () -> Void
    let onRefresh: () -> Void
    let onSearch: (WantedEpisode) -> Void

    var body: some View {
        let items = state.items as! [WantedEpisode]
        if state.isLoading && items.isEmpty {
            Spacer()
            ProgressView()
            Spacer()
        } else if items.isEmpty {
            Spacer()
            Text(MR.strings().bazarr_no_wanted_episodes.localized())
                .foregroundStyle(.secondary)
            Spacer()
        } else {
            List {
                ForEach(items, id: \.sonarrEpisodeId) { episode in
                    WantedRow(
                        title: episode.seriesTitle,
                        subtitle: "\(episode.episodeNumber) · \(episode.episodeTitle)",
                        missing: episode.missingSubtitles,
                        onSearch: { onSearch(episode) }
                    )
                    .onAppear {
                        if episode.sonarrEpisodeId == items.last?.sonarrEpisodeId {
                            onLoadMore()
                        }
                    }
                }
            }
            .listStyle(.plain)
            .refreshable { onRefresh() }
        }
    }
}

private struct WantedMoviesList: View {
    let state: PagedData<WantedMovie>
    let onLoadMore: () -> Void
    let onRefresh: () -> Void
    let onSearch: (WantedMovie) -> Void

    var body: some View {
        let items = state.items as! [WantedMovie]
        if state.isLoading && items.isEmpty {
            Spacer()
            ProgressView()
            Spacer()
        } else if items.isEmpty {
            Spacer()
            Text(MR.strings().bazarr_no_wanted_movies.localized())
                .foregroundStyle(.secondary)
            Spacer()
        } else {
            List {
                ForEach(items, id: \.radarrId) { movie in
                    WantedRow(
                        title: movie.title,
                        subtitle: nil,
                        missing: movie.missingSubtitles,
                        onSearch: { onSearch(movie) }
                    )
                    .onAppear {
                        if movie.radarrId == items.last?.radarrId {
                            onLoadMore()
                        }
                    }
                }
            }
            .listStyle(.plain)
            .refreshable { onRefresh() }
        }
    }
}

private struct WantedRow: View {
    let title: String
    let subtitle: String?
    let missing: [BazarrSubtitleLanguage]
    let onSearch: () -> Void

    var body: some View {
        HStack(alignment: .center) {
            VStack(alignment: .leading, spacing: 4) {
                Text(title).font(.headline)
                if let subtitle {
                    Text(subtitle).font(.subheadline).foregroundStyle(.secondary)
                }
                HStack(spacing: 6) {
                    ForEach(Array(missing.enumerated()), id: \.offset) { _, language in
                        SubtitleChip(label: chipLabel(language))
                    }
                }
            }
            Spacer()
            Button(action: onSearch) {
                Image(systemName: "magnifyingglass")
            }
            .buttonStyle(.bordered)
        }
    }
}

private struct ProvidersList: View {
    let state: ProvidersUiState
    let onRefresh: () -> Void
    let onReset: () -> Void

    var body: some View {
        List {
            ForEach(Array(state.providers.enumerated()), id: \.offset) { _, provider in
                HStack {
                    Circle()
                        .fill(isHealthy(provider) ? Color.green : Color.red)
                        .frame(width: 10, height: 10)
                    VStack(alignment: .leading) {
                        Text(provider.name).font(.headline)
                        if let status = provider.status, !status.isEmpty {
                            Text(status).font(.subheadline).foregroundStyle(.secondary)
                        }
                    }
                    Spacer()
                }
            }
        }
        .listStyle(.plain)
        .refreshable { onRefresh() }
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(MR.strings().bazarr_reset_providers.localized(), action: onReset)
            }
        }
    }

    private func isHealthy(_ provider: ProviderStatus) -> Bool {
        guard let status = provider.status, !status.isEmpty else { return true }
        return status.caseInsensitiveCompare("good") == .orderedSame
    }
}

struct SubtitleChip: View {
    let label: String
    var body: some View {
        Text(label)
            .font(.caption)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(Color.secondary.opacity(0.2))
            .clipShape(Capsule())
    }
}

func chipLabel(_ language: BazarrSubtitleLanguage) -> String {
    var label = language.code2.isEmpty ? language.name : language.code2.uppercased()
    if language.forced { label += " · Forced" }
    if language.hi { label += " · HI" }
    return label
}
