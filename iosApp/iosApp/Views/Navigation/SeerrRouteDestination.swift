//
//  SeerrRouteDestination.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-04-24.
//

import SwiftUI
import Shared

struct SeerrRouteDestination: View {
    let route: SeerrRoute
    
    var body: some View {
        switch route {
        case .details(let tmdbId, let requestType):
            SeerrDetailsScreen(tmdbId: tmdbId, requestType: requestType)
        }
    }
}
