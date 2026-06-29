//
//  BazarrRouteDestination.swift
//  iosApp
//

import SwiftUI
import Shared

struct BazarrRouteDestination: View {
    let route: BazarrRoute
    
    var body: some View {
        switch route {
        case .library:
            BazarrTabContent()
        case .details(let id, let type):
            BazarrDetailsScreen(id: id, type: type)
        default:
            EmptyView()
        }
    }
}
