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
                MediaRouteDestination(route: value)
            }
    }
}
