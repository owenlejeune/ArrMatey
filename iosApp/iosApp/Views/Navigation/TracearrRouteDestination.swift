import SwiftUI
import Shared

struct TracearrRouteDestination: View {
    let route: TracearrRoute
    @StateObject private var instancesViewModel = InstancesViewModelS(types: [.tracearr, .tautulli])

    var body: some View {
        let isTautulli = instancesViewModel.instancesState.selectedInstance?.type == .tautulli

        switch route {
        case .history:
            if isTautulli {
                MonitoringHistoryScreen()
            } else {
                TracearrHistoryScreen()
            }
        case .user(let userRef):
            TracearrUserScreen(userRef: userRef)
        case .users:
            if isTautulli {
                TautulliUsersScreen()
            } else {
                TracearrUsersScreen()
            }
        case .violations:
            TracearrViolationsScreen()
        case .activity:
            TracearrActivityScreen()
        case .stats:
            TautulliStatsScreen()
        }
    }
}
