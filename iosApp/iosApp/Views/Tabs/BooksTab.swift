//
//  BooksTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-02.
//

import Shared
import SwiftUI

struct BooksTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject private var booksViewModel = ArrMediaViewModelS(type: .booksehelf)
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.bookPath) {
                BooksTabContent(viewModel: booksViewModel)
            }
        case .launcher:
            BooksTabContent(viewModel: booksViewModel)
        }
    }
}

struct BooksTabContent: View {
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject var viewModel: ArrMediaViewModelS
    
    var body: some View {
        ArrTab(type: .booksehelf, viewModel: viewModel)
            .navigationDestination(for: MediaRoute.self) { value in
                destination(for: value)
            }
    }
    
    @ViewBuilder
    private func destination(for route: MediaRoute) -> some View {
        switch route {
        case .details(let id, _):
            MediaDetailsScreen(id: id, type: .booksehelf)
        case .search(let query, _):
            MediaSearchScreen(query: query, type: .booksehelf)
        case .preview(let json, _):
            MediaPreviewScreen(json: json, type: .booksehelf)
            
            // unused
        case .movieFiles(_):
            EmptyView()
        case .movieRelease(_):
            EmptyView()
        case .seriesReleases(_, _, _):
            EmptyView()
        case .episodeDetails(_, _):
            EmptyView()
        case .albumReleases(let albumId, let artistId):
            EmptyView()
        }
    }
}
