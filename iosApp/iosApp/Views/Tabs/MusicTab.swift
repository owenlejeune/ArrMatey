//
//  MusicTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-12.
//

import SwiftUI
import Shared

struct MusicTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject private var musicViewModel = ArrMediaViewModelS(type: .lidarr)
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.musicPath) {
                MusicTabContent(viewModel: musicViewModel)
            }
        case .launcher:
            MusicTabContent(viewModel: musicViewModel)
        }
    }
}

struct MusicTabContent: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject var viewModel: ArrMediaViewModelS
    
    var body: some View {
        ArrTab(type: .lidarr, viewModel: viewModel)
            .navigationDestination(for: MediaRoute.self) { value in
                destination(for: value)
            }
    }
    
    @ViewBuilder
    private func destination(for route: MediaRoute) -> some View {
        switch route {
        case .details(let id, _):
            MediaDetailsScreen(id: id, type: .lidarr)
        case .search(let query, _):
            MediaSearchScreen(query: query, type: .lidarr)
        case .preview(let json, _):
            MediaPreviewScreen(json: json, type: .lidarr)
        case .albumReleases(let albumId, let artistId):
            let releaseParams = ReleaseParamsAlbum(albumId: albumId, artistId: artistId?.asKotlinLong)
            InteractiveSearchScreen(type: .lidarr, releaseParams: releaseParams)
            
        // unused
        case .movieFiles(_):
            EmptyView()
        case .movieRelease(_):
            EmptyView()
        case .seriesReleases(_, _, _):
            EmptyView()
        case .episodeDetails(_, _):
            EmptyView()
        }
    }
}
