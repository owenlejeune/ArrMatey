import SwiftUI
import Shared

struct TracearrRouteDestination: View {
    let route: TracearrRoute

    var body: some View {
        switch route {
        case .history:
            TracearrHistoryScreen()
        case .user(let userRef):
            TracearrUserScreen(userRef: userRef)
        }
    }
}
