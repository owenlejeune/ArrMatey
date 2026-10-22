//
//  DownloadClientDashboardViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class DownloadClientDashboardViewModelS: ObservableObject {
    private let viewModel: DownloadClientDashboardViewModel
    
    @Published private(set) var state: DownloadClientDashboardUiState = DownloadClientDashboardUiStateInitial()
    @Published private(set) var isRefreshing: Bool = false
    @Published private(set) var client: DownloadClient? = nil
    
    init(_ id: Int64) {
        self.viewModel = KoinBridge.shared.getDownloadClientDashboardViewModel(clientId: id)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.state.observeAsync(on: self, to: \.state)
        viewModel.isRefreshing.observeAsync(on: self) { owner, isRefreshing in
            owner.isRefreshing = isRefreshing.boolValue
        }
        viewModel.client.observeAsync(on: self, to: \.client)
    }
    
    func refresh() {
        viewModel.refresh()
    }
    
    func deleteClient() {
        viewModel.deleteClient()
    }
}
