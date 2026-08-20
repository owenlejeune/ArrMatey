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
                MediaRouteDestination(route: value)
            }
    }
}
