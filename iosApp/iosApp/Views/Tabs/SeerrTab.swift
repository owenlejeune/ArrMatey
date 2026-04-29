//
//  RequestsTab.swift
//  iosApp
//

import SwiftUI
import Shared

struct RequestsTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.seerrPath) {
                SeerrTabContent()
                    .navigationDestination(for: SeerrRoute.self) { route in
                        seerrDestination(for: route)
                    }
            }
        case .launcher:
            SeerrTabContent()
                .navigationDestination(for: SeerrRoute.self) { route in
                    seerrDestination(for: route)
                }
        }
    }
    
    @ViewBuilder
    private func seerrDestination(for route: SeerrRoute) -> some View {
        switch route {
        case .details(let tmdbId, let requestType):
            SeerrDetailsScreen(tmdbId: tmdbId, requestType: requestType)
        }
    }
}
