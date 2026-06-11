//
//  DashboardViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-06-11.
//

import Shared
import SwiftUI

@MainActor
class DashboardViewModelS: ObservableObject {
    private let viewModel: CombinedDashboardViewModel
    
    @Published private(set) var isRefreshing: Bool = false
    @Published private(set) var state: CombinedDashboardState = CombinedDashboardStateInitial()
    @Published private(set) var isEditing: Bool = false
    
    init() {
        self.viewModel = KoinBridge.shared.getDashboardViewModel()
        startObserving()
    }
    
    private func startObserving() {
        
    }
    
}
