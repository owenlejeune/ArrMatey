//
//  EpisodeDetailsScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-24.
//

import SwiftUI
import Shared

struct EpisodeDetailsScreen: View {
    private let series: ArrSeries
    
    @ObservedObject private var viewModel: EpisodeDetailsViewModelS
    
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject private var navigation: NavigationManager
    
    @State private var confirmDelete: Bool = false
    
    private var episode: Episode {
        viewModel.episode
    }
    
    init(seriesJson: String, episodeJson: String) {
        self.series = ArrMediaCompanion().fromJson(value: seriesJson) as! ArrSeries
        
        let episode = Episode.companion.fromJson(json: episodeJson)
        self.viewModel = EpisodeDetailsViewModelS(seriesId: series.id as! Int64, episode: episode)
    }
    
    var body: some View {
        contentForState()
            .toolbar { toolbarContent }
            .alert(MR.strings().are_you_sure.localized(), isPresented: $confirmDelete) {
                Button(MR.strings().yes.localized(), role: .destructive) {
                    viewModel.deleteEpisode()
                    confirmDelete = false
                }
                Button(MR.strings().no.localized(), role: .cancel) {
                    confirmDelete = false
                }
            } message: {
                Text(MR.strings().episode_delete_message.localized())
            }
    }
    
    @ViewBuilder
    private func contentForState() -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                EpisodeDetailsHeader(series: series, episode: episode)
                
                VStack(alignment: .leading, spacing: 12) {
                    Text(episode.displayTitle)
                        .font(.title)
                        .bold()

                    ItemDescriptionCard(overview: episode.overview)
                    
                    ReleaseDownloadButtons(
                        onInteractiveClicked: {
                            navigation.go(to: .seriesReleases(episodeId: episode.id), of: .sonarr)
                        },
                        automaticSearchEnabled: viewModel.episode.monitored,
                        onAutomaticClicked: {
                            viewModel.executeAutomaticSearch()
                        })
                    
                    Text(MR.strings().files.localized())
                        .font(.system(size: 20, weight: .bold))
                    
                    if let file = episode.episodeFile {
                        MediaFileCard(file: file)
                    }

                    BazarrSubtitlesSection(
                        target: BazarrMediaTargetEpisode(
                            seriesId: series.id as! Int64,
                            episodeId: episode.id
                        )
                    )

                    switch viewModel.history {
                    case is HistoryStateLoading:
                        ProgressView()
                            .progressViewStyle(.circular)
                    case let success as HistoryStateSuccess:
                        if success.items.isEmpty {
                            Text(MR.strings().no_history.localized())
                                .font(.system(size: 16, weight: .medium))
                                .frame(maxWidth: .infinity, alignment: .center)
                        } else {
                            Text(MR.strings().history.localized())
                                .font(.system(size: 20, weight: .bold))
                            ForEach(success.items, id: \.id) { historyItem in
                                HistoryItemView(item: historyItem)
                            }
                        }
                    default:
                        EmptyView()
                    }
                    
                    Spacer()
                        .frame(height: 12)
                }
                .padding(.horizontal, 24)
            }
            .frame(alignment: .top)
        }
        .ignoresSafeArea(edges: .top)
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .primaryAction) {
            Image(systemName: viewModel.episode.monitored ? "bookmark.fill" : "bookmark")
                .imageScale(.medium)
                .onTapGesture {
                    viewModel.toggleMonitor()
                }
        }
        ToolbarItem(placement: .primaryAction) {
            Image(systemName: "trash")
                .imageScale(.medium)
                .tint(.red)
                .onTapGesture {
                    confirmDelete = true
                }
        }
    }
}
