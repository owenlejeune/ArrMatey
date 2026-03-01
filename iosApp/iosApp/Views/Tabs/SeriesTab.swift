//
// Created by Owen LeJeune on 2025-11-20.
//

import Foundation
import SwiftUI
import Shared

struct SeriesTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject private var seriesViewModel = ArrMediaViewModelS(type: .sonarr)
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.seriesPath) {
                SeriesTabContent(viewModel: seriesViewModel)
            }
        case .launcher:
            SeriesTabContent(viewModel: seriesViewModel)
        }
    }
}

struct SeriesTabContent: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject var viewModel: ArrMediaViewModelS
    
    var body: some View {
        ArrTab(type: .sonarr, viewModel: viewModel)
            .navigationDestination(for: MediaRoute.self) { value in
                destination(for: value)
            }
    }
    
    @ViewBuilder
    private func destination(for route: MediaRoute) -> some View {
        switch route {
        case .details(let id, _):
            MediaDetailsScreen(id: id, type: .sonarr)
        case .search(let query, _):
            MediaSearchScreen(query: query, type: .sonarr)
        case .preview(let json, _):
            MediaPreviewScreen(json: json, type: .sonarr)
        case .seriesReleases(let seriesId, let seasonNumber, let episodeId):
            let releaseParams = ReleaseParamsSeries(seriesId: seriesId?.asKotlinLong, seasonNumber: seasonNumber?.asKotlinInt, episodeId: episodeId?.asKotlinLong)
            let defaultFilter: ReleaseFilterBy = if episodeId != nil { .singleEpisode } else { .seasonPack }
            InteractiveSearchScreen(type: .sonarr, releaseParams: releaseParams, defaultFilter: defaultFilter)
        case .episodeDetails(let seriesJson, let episodeJson):
            EpisodeDetailsScreen(seriesJson: seriesJson, episodeJson: episodeJson)
            
        // unused
        case .movieFiles(_):
            EmptyView()
        case .movieRelease(_):
            EmptyView()
        case .albumReleases(_, _):
            EmptyView()
        }
    }
}
