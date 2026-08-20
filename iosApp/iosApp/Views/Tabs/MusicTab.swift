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
                MediaRouteDestination(route: value)
            }
    }
}
