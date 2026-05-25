//
//  AudiobooksTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-19.
//

import Shared
import SwiftUI

struct AudiobooksTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject private var booksViewModel = ArrMediaViewModelS(type: .listenarr)
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.audiobookPath) {
                AudiobooksTabContent(viewModel: booksViewModel)
            }
        case .launcher:
            AudiobooksTabContent(viewModel: booksViewModel)
        }
    }
}

struct AudiobooksTabContent: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject var viewModel: ArrMediaViewModelS
    
    var body: some View {
        ArrTab(type: .listenarr, viewModel: viewModel)
            .navigationDestination(for: MediaRoute.self) { value in
                destination(for: value)
            }
    }
    
    @ViewBuilder
    private func destination(for route: MediaRoute) -> some View {
        switch route {
        case .details(let id, _):
            MediaDetailsScreen(id: id, type: .listenarr)
        case .search(let query, _):
            MediaSearchScreen(query: query, type: .listenarr)
        case .preview(let json, _):
            MediaPreviewScreen(json: json, type: .listenarr)
        case .audiobookReleases(let id, let query):
            let releaseParams = ReleaseParamsAudiobook(mediaId: id?.asKotlinLong, query: query)
            InteractiveSearchScreen(type: .listenarr, releaseParams: releaseParams)
        case .audiobookFiles(let audiobookJson):
            AudiobookFilesScreen(audiobookJson: audiobookJson)
            
            // unused
        default:
            EmptyView()
        }
    }
}
