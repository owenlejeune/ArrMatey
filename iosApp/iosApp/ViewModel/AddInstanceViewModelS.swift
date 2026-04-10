//
//  AddInstanceViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-19.
//

import Shared
import SwiftUI

@MainActor
class AddInstanceViewModelS: ObservableObject {
    private let viewModel: AddInstanceViewModel
    
    @Published private(set) var uiState: AddInstanceUiState
    @Published var showError: Bool = false
    @Published var createWasSuccessful: Bool = false
    
    init() {
        self.viewModel = KoinBridge.shared.getAddInstanceViewModel()
        self.uiState = AddInstanceUiState()
        startObserving()
    }
    
    private func startObserving() {
        viewModel.uiState.observeAsync {
            self.uiState = $0
            self.showError = $0.createResult is InsertResultError
            self.createWasSuccessful = $0.createResult is InsertResultSuccess
        }
    }
    
    func setApiEndpoint(_ endpoint: String) {
        viewModel.setApiEndpoint(endpoint: endpoint)
    }
    
    func setApiKey(_ key: String) {
        viewModel.setApiKey(value: key)
    }
    
    func setIsSlowInstance(_ isSlowInstance: Bool) {
        viewModel.setIsSlowInstance(value: isSlowInstance)
    }
    
    func setCustomTimeout(_ customTimeout: Int64?) {
        viewModel.setCustomTimeout(value: customTimeout?.asKotlinLong)
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
    
    func setLocalNetworkSsid(_ ssid: String) {
        viewModel.setLocalNetworkSsid(ssid: ssid)
    }
    
    func toggleNotificationsEnabled() {
        viewModel.toggleNotificationsEnabled()
    }
    
    func reset() {
        viewModel.reset()
    }
    
    func dismissInfoCard(_ type: InstanceType) {
        viewModel.dismissInfoCard(instanceType: type)
    }
    
    func testConnection(_ type: InstanceType) {
        viewModel.testConnection(type: type)
    }
    
    func testLocalConnection(_ type: InstanceType) {
        viewModel.testLocalConnection(type: type)
    }
    
    func createInstance(_ type: InstanceType) {
        viewModel.createInstance(type: type)
    }
}
