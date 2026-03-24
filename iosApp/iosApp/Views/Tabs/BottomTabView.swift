//
// Created by Owen LeJeune on 2025-11-20.
//

import Foundation
import Shared
import SwiftUI

struct BottomTabView: View {
    @EnvironmentObject var navigationManager: NavigationManager
    var tabItem: TabItem

    var body: some View {
        NavigationStack {
            TabItemContent(tabItem: tabItem)
                .toolbar {
                    ToolbarItem(placement: .topBarLeading) {
                        Button {
                            navigationManager.openSettings()
                        } label: {
                            Image(systemName: "gear")
                        }
                    }
                }
        }
    }
}
