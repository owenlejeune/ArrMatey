//
//  MoviesTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-03.
//

import Foundation
import SwiftUI
import Shared

struct MoviesTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject private var movieViewModel = ArrMediaViewModelS(type: .radarr)
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.moviePath) {
                MoviesTabContent(viewModel: movieViewModel)
            }
        case .launcher:
            MoviesTabContent(viewModel: movieViewModel)
        }
    }
}

struct MoviesTabContent: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject var viewModel: ArrMediaViewModelS
    
    var body: some View {
        ArrTab(type: .radarr, viewModel: viewModel)
            .navigationDestination(for: MediaRoute.self) { value in
                destination(for: value)
            }
    }
    
    @ViewBuilder
    private func destination(for route: MediaRoute) -> some View {
        switch route {
        case .details(let id, _):
            MediaDetailsScreen(id: id, type: .radarr)
        case .search(let query, _):
            MediaSearchScreen(query: query, type: .radarr)
        case .preview(let json, _):
            MediaPreviewScreen(json: json, type: .radarr)
        case .movieFiles(let json):
            MovieFilesScreen(json: json)
        case .movieRelease(let id):
            let releaseParams = ReleaseParamsMovie(movieId: id)
            InteractiveSearchScreen(type: .radarr, releaseParams: releaseParams)
            
        // unused
        case .seriesReleases(_, _, _):
            EmptyView()
        case .episodeDetails(_, _):
            EmptyView()
        case .albumReleases(_, _):
            EmptyView()
        }
    }
}
