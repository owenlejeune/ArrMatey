//
//  BazarrDetailsScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct BazarrDetailsScreen: View {
    private let id: Int64
    private let type: BazarrMediaType
    
    @ObservedObject private var viewModel: BazarrDetailsViewModelS
    @State private var searchTarget: SearchTarget?

    init(id: Int64, type: BazarrMediaType) {
        self.id = id
        self.type = type
        self.viewModel = BazarrDetailsViewModelS(id: id, type: type)
    }

    var body: some View {
        Group {
            if let details = viewModel.uiState.details {
                List {
                    Section {
                        BazarrDetailsHeader(details: details)
                            .listRowInsets(EdgeInsets())
                    }
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)
                    
                    Section {
                        VStack(alignment: .leading, spacing: 24) {
                            Text(details.title)
                                .font(.system(size: 28, weight: .bold))
                                .frame(maxWidth: .infinity, alignment: .leading)
                            
                            ItemDescriptionCard(overview: details.overview)
                        }
                        .padding(.top, 12)
                    }
                    .listRowInsets(EdgeInsets(top: 0, leading: 24, bottom: 0, trailing: 24))
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)
                    
                    Section {
                        if let movie = details as? BazarrMovie {
                            BazarrSubtitlesSection(target: BazarrMediaTargetMovie(radarrId: movie.serviceId))
                        } else if details is BazarrSeries {
                            let episodes = viewModel.uiState.episodes as? [BazarrEpisode] ?? []
                            BazarrEpisodesSection(episodes: episodes) { seriesId, epId in
                                searchTarget = SearchTarget(target: BazarrMediaTargetEpisode(seriesId: seriesId, episodeId: epId))
                            }
                        }
                    }
                    .listRowInsets(EdgeInsets(top: 12, leading: 24, bottom: 12, trailing: 24))
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)
                    
                    BazarrInfoArea(details: details)
                        .listRowInsets(EdgeInsets(top: 12, leading: 24, bottom: 24, trailing: 24))
                        .listRowSeparator(.hidden)
                        .listRowBackground(Color.clear)
                }
                .listStyle(.plain)
                .scrollContentBackground(.hidden)
                .ignoresSafeArea(edges: .top)
            } else {
                ProgressView()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    viewModel.performSearch()
                } label: {
                    if viewModel.operationState is OperationStatusInProgress {
                        ProgressView()
                    } else {
                        Image(systemName: "magnifyingglass")
                    }
                }
                .disabled(viewModel.operationState is OperationStatusInProgress)
            }
        }
        .sheet(item: $searchTarget) { item in
            BazarrSubtitleSearchSheet(target: item.target)
        }
    }
}

private struct SearchTarget: Identifiable {
    let id = UUID()
    let target: BazarrMediaTarget
}

private struct BazarrDetailsHeader: View {
    let details: BazarrMedia

    var body: some View {
        ZStack(alignment: .topLeading) {
            MediaHeaderBanner(bannerUrl: URL(string: details.fanart ?? ""))
            
            // Gradient Overlay
            VStack(spacing: 0) {
                Spacer()
                LinearGradient(
                    gradient: Gradient(colors: [
                        .clear,
                        Color(.systemBackground).opacity(0.8),
                        Color(.systemBackground)
                    ]),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .frame(height: 150)
            }
            .frame(height: 400)
            
            HStack(alignment: .bottom, spacing: 24) {
                GenericPosterItem(posterUrl: details.poster)
                    .frame(width: 125)
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
            .padding(.top, 170)
        }
        .frame(height: 400)
    }
}

private struct BazarrEpisodesSection: View {
    let episodes: [BazarrEpisode]
    let onSearch: (Int64, Int64) -> Void

    var body: some View {
        let seasons = Dictionary(grouping: episodes, by: { $0.season }).sorted(by: { $0.key > $1.key })

        VStack(alignment: .leading, spacing: 12) {
            Text(MR.strings().seasons_header.localized())
                .font(.title2)
                .bold()

            ForEach(seasons, id: \.key) { season, seasonEpisodes in
                DisclosureGroup {
                    VStack(alignment: .leading, spacing: 8) {
                        ForEach(seasonEpisodes.sorted(by: { $0.episode < $1.episode }), id: \.sonarrEpisodeId) { episode in
                            Button {
                                onSearch(episode.sonarrSeriesId, episode.sonarrEpisodeId)
                            } label: {
                                BazarrEpisodeRow(episode: episode)
                            }
                            .buttonStyle(.plain)
                            Divider()
                        }
                    }
                    .padding(.vertical, 8)
                } label: {
                    Text(season == 0 ? MR.strings().specials.localized() : MR.strings().season_label.formatted(args: [season]))
                        .font(.headline)
                        .foregroundColor(.primary)
                }
                .padding()
                .background(Color.secondary.opacity(0.1))
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
    }
}

private struct BazarrEpisodeRow: View {
    let episode: BazarrEpisode

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text("\(episode.episode). ").foregroundColor(.themePrimary) +
                Text(episode.title).fontWeight(.medium)
                Spacer()
                if episode.subtitles.isEmpty {
                    Text(MR.strings().missing.localized())
                        .font(.caption)
                        .italic()
                        .foregroundColor(.red)
                }
            }

            FlowLayout(spacing: 6) {
                ForEach(Array(episode.audioLanguages.enumerated()), id: \.offset) { _, lang in
                    SubtitleChip(label: lang.name.uppercased())
                }

                ForEach(Array(Set(episode.subtitles.map { "\($0.code2.uppercased())\($0.hi ? ":HI" : "")" }).sorted()), id: \.self) { sub in
                    Text(sub)
                        .font(.caption2)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color.secondary.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                }
            }
        }
    }
}
