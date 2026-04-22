//
//  EditInstanceViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-20.
//

import Shared
import SwiftUI

@MainActor
class EditInstanceViewModelS: ObservableObject {
    private let viewModel: EditInstanceViewModel
    
    @Published private(set) var uiState: AddInstanceUiState
    @Published private(set) var instance: Instance? = nil
    @Published var showError: Bool = false
    
    init(_ id: Int64) {
        self.viewModel = KoinBridge.shared.getEditInstanceViewModel(instanceId: id)
        self.uiState = AddInstanceUiState()
        startObserving()
    }
    
    private func startObserving() {
        viewModel.uiState.observeAsync {
            self.uiState = $0
            self.showError = $0.createResult is InsertResultSuccess
        }
        viewModel.instance.observeAsync { self.instance = $0}
    }
    
    func setApiEndpoint(_ endpoint: String) {
        viewModel.setApiEndpoint(endpoint: endpoint)
    }
    
    func setApiKey(_ key: String) {
        viewModel.setApiKey(value: key)
    }
    
    func setBasicAuthEnabled(_ enabled: Bool) {
        viewModel.setBasicAuthEnabled(enabled: enabled)
    }
    
    func setIsSlowInstance(_ isSlowInstance: Bool) {
        viewModel.setIsSlowInstance(value: isSlowInstance)
    }
    
    func setCustomTimeout(_ customTimeout: Int64?) {
        viewModel.setCustomTimeout(
            value: customTimeout.map(KotlinLong.init(value:))
        )
    }
    
    func setInstanceLabel(_ instanceLabel: String) {
        viewModel.setInstanceLabel(value: instanceLabel)
    }
    
    func updateHeaders(_ headers: [InstanceHeader]) {
        viewModel.updateHeaders(headers: headers)
    }
    
    func setLocalNetworkEnabled(_ enabled: Bool) {
        viewModel.setLocalNetworkEnabled(enabled: enabled)
    }
    
    func setLocalNetworkUrl(_ url: String) {
        viewModel.setLocalNetworkUrl(url: url)
    }
    
    func setLocalNetworkSsids(_ ssids: [String]) {
        viewModel.setLocalNetworkSsids(ssids: ssids)
    }
    
    func toggleNotificationsEnabled() {
        viewModel.toggleNotificationsEnabled()
    }
    
    func reset() {
        viewModel.reset()
    }
    
    func testConnection() {
        viewModel.testConnection()
    }
    
    func testLocalConnection() {
        viewModel.testLocalConnection()
    }
    
    func updateInstance() {
        viewModel.updateInstance()
    }
    
    func delete(_ instance: Instance) {
        viewModel.deleteInstance(instance: instance)
    }
}
