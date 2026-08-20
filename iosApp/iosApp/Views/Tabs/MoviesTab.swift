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
                MediaRouteDestination(route: value)
            }
    }
}
