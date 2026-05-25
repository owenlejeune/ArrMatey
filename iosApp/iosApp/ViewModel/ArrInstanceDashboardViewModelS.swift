//
//  ArrInstanceDashboardViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-20.
//

import Shared
import SwiftUI

@MainActor
class ArrInstanceDashboardViewModelS: ObservableObject {
    private let viewModel: ArrInstanceDashboardViewModel
    
    @Published private(set) var state: ArrDashboardState = ArrDashboardStateInitial()
    @Published private(set) var isRefreshing: Bool = false
    @Published private(set) var instance: Instance? = nil
    
    init(_ id: Int64) {
        self.viewModel = KoinBridge.shared.getArrInstanceDashboardViewModel(instanceId: id)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.state.observeAsync(on: self, to: \.state)
        viewModel.isRefreshing.observeAsync(on: self) { owner, isRefreshing in
            owner.isRefreshing = isRefreshing.boolValue
        }
        viewModel.instance.observeAsync(on: self, to: \.instance)
    }
    
    func refresh() {
        viewModel.refresh()
    }
    
}
