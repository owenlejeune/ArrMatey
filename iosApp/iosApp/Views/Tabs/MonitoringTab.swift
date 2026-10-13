import Shared
import SwiftUI

struct MonitoringTab: View {
    var body: some View {
        MonitoringTabContent()
    }
}

struct MonitoringTabContent: View {
    @StateObject private var instancesViewModel = InstancesViewModelS(types: [.tracearr, .tautulli])
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        Group {
            if let selected = instancesViewModel.instancesState.selectedInstance {
                switch selected.type {
                case .tracearr:
                    TracearrTabContent()
                case .tautulli:
                    TautulliHomeScreen()
                default:
                    NoInstanceView(type: .tracearr)
                }
            } else if instancesViewModel.instancesState.instances.isEmpty {
                NoInstanceView(type: .tracearr)
            } else {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
    }
}
