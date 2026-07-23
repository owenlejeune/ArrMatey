//
//  BazarrTab.swift
//  iosApp
//

import SwiftUI
import Shared

struct BazarrTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigation: NavigationManager

    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigation.bazarrPath) {
                BazarrTabContent()
                    .navigationDestination(for: BazarrRoute.self) { route in
                        BazarrRouteDestination(route: route)
                    }
                    .navigationDestination(for: AnyTabItem.self) { anyTabItem in
                        let tab: TabItem = anyTabItem.item
                        TabItemContent(tabItem: tab)
                    }
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
    @State private var searchTarget: SearchTarget?
    @StateObject private var viewModel = BazarrViewModelS()
    
    @EnvironmentObject private var navigation: NavigationManager

    var body: some View {
        VStack(spacing: 0) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(BazarrSection.allCases, id: \.self) { section in
                        let isSelected = viewModel.selectedSection == section
                        Text(sectionLabel(section))
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)
                            .background(isSelected ? Color.themePrimary : Color.secondary.opacity(0.1))
                            .foregroundStyle(isSelected ? .white : .primary)
                            .clipShape(Capsule())
                            .onTapGesture {
                                viewModel.selectSection(section)
                            }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
            }

            let state = viewModel.uiState
            if state is BazarrLibraryInitial || state is BazarrLibraryLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if let error = state as? BazarrLibraryError {
                Spacer()
                Text(error.message).foregroundStyle(.red)
                Spacer()
            } else if let success = state as? BazarrLibrarySuccess {
                Group {
                    switch viewModel.selectedSection {
                    case .series:
                        let series = success.series
                        BazarrSeriesList(series: series, onClick: { item in
                            navigation.go(to: BazarrRoute.details(item.serviceId, .series))
                        })
                    case .movies:
                        let movies = success.movies
                        BazarrMoviesList(movies: movies, onClick: { item in
                            navigation.go(to: BazarrRoute.details(item.serviceId, .movie))
                        })
                    case .wantedEpisodes:
                        let episodes = success.wantedEpisodes
                        WantedEpisodesList(items: episodes, onSearch: { episode in
                            searchTarget = SearchTarget(
                                target: BazarrMediaTargetEpisode(
                                    seriesId: episode.sonarrSeriesId,
                                    episodeId: episode.sonarrEpisodeId
                                )
                            )
                        })
                    case .wantedMovies:
                        let movies = success.wantedMovies
                        WantedMoviesList(items: movies, onSearch: { movie in
                            searchTarget = SearchTarget(
                                target: BazarrMediaTargetMovie(radarrId: movie.radarrId)
                            )
                        })
                    case .providers:
                        ProvidersList(
                            providers: success.providers as? [ProviderStatus] ?? [],
                            onReset: viewModel.resetProviders
                        )
                    default:
                        EmptyView()
                    }
                }
                .refreshable { viewModel.refresh() }
            }
        }
        .navigationTitle(MR.strings().bazarr.localized())
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                    navigation.showLauncher = true
                } label: {
                    Image(systemName: "line.3.horizontal")
                }
            }
        }
        .searchable(text: $viewModel.searchQuery)
        .onChange(of: viewModel.searchQuery) { _, newValue in
            viewModel.updateSearchQuery(newValue)
        }
        .sheet(item: $searchTarget) { item in
            BazarrSubtitleSearchSheet(target: item.target)
        }
    }

    private func sectionLabel(_ section: BazarrSection) -> String {
        let success = viewModel.uiState as? BazarrLibrarySuccess
        let count: Int32 = {
            guard let success else { return 0 }
            switch section {
            case .series: return Int32(success.series.count)
            case .movies: return Int32(success.movies.count)
            case .wantedEpisodes: return Int32(success.wantedEpisodes.count)
            case .wantedMovies: return Int32(success.wantedMovies.count)
            default: return 0
            }
        }()

        let label: String = {
            switch section {
            case .series: return MR.strings().series.localized()
            case .movies: return MR.strings().movies.localized()
            case .wantedEpisodes: return MR.strings().bazarr_wanted_episodes.localized()
            case .wantedMovies: return MR.strings().bazarr_wanted_movies.localized()
            case .providers: return MR.strings().bazarr_providers.localized()
            default: return ""
            }
        }()

        return count > 0 ? "\(label) (\(count))" : label
    }
}

private struct BazarrSeriesList: View {
    let series: [BazarrSeries]
    let onClick: (BazarrSeries) -> Void
    var body: some View {
        List {
            ForEach(series, id: \.serviceId) { item in
                BazarrItemRow(
                    title: item.title,
                    year: item.year,
                    overview: item.overview,
                    poster: item.poster,
                    fanart: item.fanart,
                    monitored: item.monitored,
                    details: MR.strings().bazarr_series_subtitle_count.formatted(args: [item.episodeFileCount, item.episodeFileCount + item.episodeMissingCount])
                )
                .onTapGesture { onClick(item) }
            }
        }
        .listStyle(.plain)
    }
}

private struct BazarrMoviesList: View {
    let movies: [BazarrMovie]
    let onClick: (BazarrMovie) -> Void
    var body: some View {
        List {
            ForEach(movies, id: \.serviceId) { item in
                BazarrItemRow(
                    title: item.title,
                    year: item.year,
                    overview: item.overview,
                    poster: item.poster,
                    fanart: item.fanart,
                    monitored: item.monitored,
                    details: MR.strings().bazarr_movie_subtitle_count.formatted(args: [item.subtitles.count, item.missingSubtitles.count])
                )
                .onTapGesture { onClick(item) }
            }
        }
        .listStyle(.plain)
    }
}

private struct BazarrItemRow: View {
    let title: String
    let year: String
    let overview: String
    let poster: String?
    let fanart: String?
    let monitored: Bool
    let details: String

    var body: some View {
        ZStack(alignment: .bottomLeading) {
            if let fanart {
                AsyncImage(url: URL(string: fanart)) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Color.gray.opacity(0.3)
                }
                .frame(height: 120)
                .clipped()
                .overlay(Color.black.opacity(0.5))
            } else {
                Color.gray.opacity(0.3).frame(height: 120)
            }

            HStack(alignment: .top, spacing: 12) {
                if let poster {
                    AsyncImage(url: URL(string: poster)) { image in
                        image.resizable().scaledToFit()
                    } placeholder: {
                        Color.gray
                    }
                    .frame(width: 60, height: 90)
                    .clipShape(RoundedRectangle(cornerRadius: 4))
                }

                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text("\(title) (\(year))")
                            .font(.headline)
                            .lineLimit(1)
                            .foregroundStyle(.white)
                        Spacer()
                        Image(systemName: monitored ? "bookmark.fill" : "bookmark")
                            .foregroundStyle(.white)
                    }
                    Text(details)
                        .font(.caption)
                        .foregroundStyle(.white.opacity(0.8))
                    Text(overview)
                        .font(.caption2)
                        .lineLimit(2)
                        .foregroundStyle(.white.opacity(0.6))
                }
            }
            .padding(12)
        }
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
        .listRowSeparator(.hidden)
    }
}

private struct WantedEpisodesList: View {
    let items: [WantedEpisode]
    let onSearch: (WantedEpisode) -> Void

    var body: some View {
        if items.isEmpty {
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
                }
            }
            .listStyle(.plain)
        }
    }
}

private struct WantedMoviesList: View {
    let items: [WantedMovie]
    let onSearch: (WantedMovie) -> Void

    var body: some View {
        if items.isEmpty {
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
                }
            }
            .listStyle(.plain)
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
    let providers: [ProviderStatus]
    let onReset: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Spacer()
                Button(MR.strings().bazarr_reset_providers.localized(), action: onReset)
                    .buttonStyle(.borderless)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
            }
            List {
                ForEach(Array(providers.enumerated()), id: \.offset) { _, provider in
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
