//
//  SeerrRouteDestination.swift
//  iosApp
//

import SwiftUI
import Shared

struct SeerrRouteDestination: View {
    let route: SeerrRoute
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        switch route {
        case .details(let tmdbId, let requestType):
            if requestType == .person {
                SeerrPersonDetailsScreen(personId: tmdbId)
            } else {
                UnifiedMediaDetailsScreen(tmdbId: tmdbId, requestType: requestType)
            }
        case .category(let category):
            DiscoverCategoryScreen(category: category)
        case .personDetails(let id):
            SeerrPersonDetailsScreen(personId: id)
        }
    }
}
