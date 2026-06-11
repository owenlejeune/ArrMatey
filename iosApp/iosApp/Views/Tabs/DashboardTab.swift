//
//  DashboardTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-06-11.
//

import Shared
import SwiftUI

struct DashboardTab: View {
    @Environment(\.navigationContext) private var context
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack {
                DashboardTabContent()
            }
        case .launcher:
            DashboardTabContent()
        }
    }
}

struct DashboardTabContent: View {
    
    @ObservedObject private var viewModel = DashboardViewModelS()
    
    var body: some View {
        
    }
}
