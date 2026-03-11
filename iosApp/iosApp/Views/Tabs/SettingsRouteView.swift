//
// Created by Owen LeJeune on 2025-11-20.
//

import Foundation
import SwiftUI
import Shared

struct SettingsRouteView: View {
    let route: SettingsRoute
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        switch route {
        case .newInstance(let initialType):
            NewInstanceView(initialType: initialType) {
                navigationManager.completeSetupAndDismiss()
            }
        case .dev:
            DevSettingsScreen()
        case .editInstance(let id):
            EditInstanceScreen(id: id) {
                navigationManager.completeSetupAndDismiss()
            }
        case .navigationConfig:
            TabConfigurationScreen()
        case .arrDashboard(let id):
            ArrInstanceDashboard(id: id)
        case .newDownloadClient:
            AddEditDownloadClientScreen()
        case .editDownloadClient(let id):
            AddEditDownloadClientScreen(id: id)
        }
    }
}
