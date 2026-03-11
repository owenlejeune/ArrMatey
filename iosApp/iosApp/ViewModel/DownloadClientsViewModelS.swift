//
//  DownloadClientsViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-10.
//

import Shared
import SwiftUI

@MainActor
class DownloadClientsViewModelS: ObservableObject {
    private let viewModel: DownloadClientsViewModel
    
    @Published private(set) var downloadClientsState: DownloadClientsState = DownloadClientsState()
    @Published private(set) var connectionStates: [Int64:OperationStatus] = [:]
    @Published private(set) var mutationState: DownloadClientMutationState = DownloadClientMutationStateInitial()
    
    init() {
        self.viewModel = KoinBridge.shared.getDownloadClientsViewModel()
        startObserving()
    }
    
    private func startObserving() {
        viewModel.downloadClientsState.observeAsync { self.downloadClientsState = $0 }
        viewModel.connectionStates.observeAsync { self.connectionStates = $0.reduce(into: [Int64: OperationStatus]()) { result, entry in
            let key = Int64(truncating: entry.key)
            result[key] = entry.value
        } }
        viewModel.mutationState.observeAsync { self.mutationState = $0 }
    }
    
    func testConnection(_ id: Int64) {
        viewModel.testConnection(id: id)
    }
    
    func deleteClient(_ client: DownloadClient) {
        viewModel.deleteClient(downloadClient: client)
    }
    
    func resetMutationState() {
        viewModel.resetMutationState()
    }
    
    func setClientActive(_ client: DownloadClient) {
        viewModel.setClientActive(downloadClient: client)
    }
}
