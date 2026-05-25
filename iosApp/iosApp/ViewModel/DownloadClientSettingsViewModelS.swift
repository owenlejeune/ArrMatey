//
//  DownloadClientSettingsViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class DownloadClientSettingsViewModelS: ObservableObject {
    private let viewModel: DownloadClientSettingsViewModel
    
    @Published private(set) var uiState: DownloadClientConfigurationUiState = DownloadClientConfigurationUiState()
    @Published private(set) var downloadClient: DownloadClient? = nil
    
    @Published private(set) var mutationSuccess: Bool = false

    init(id: Int64? = nil) {
        self.viewModel = KoinBridge.shared.getDownloadClientSettingsViewModel(clientId: id as? KotlinLong)
        startObserving()
    }

    private func startObserving() {
        viewModel.uiState.observeAsync(on: self) { owner, state in
            owner.uiState = state
            owner.mutationSuccess = state.mutationState is DownloadClientMutationStateSuccess
        }
        viewModel.downloadClient.observeAsync(on: self, to: \.downloadClient)
    }

    func updateLabel(_ label: String) {
        viewModel.updateLabel(label: label)
    }
    
    func updateSelectedType(_ type: DownloadClientType) {
        viewModel.updateSelectedType(type: type)
    }
    
    func updateUrl(_ url: String) {
        viewModel.updateUrl(url: url)
    }
    
    func updateUsername(_ username: String) {
        viewModel.updateUsername(username: username)
    }
    
    func updatePassword(_ password: String) {
        viewModel.updatePassword(password: password)
    }
    
    func updateApiKey(_ apiKey: String) {
        viewModel.updateApiKey(apiKey: apiKey)
    }
    
    func updateNoApiKeyRequired(_ enabled: Bool) {
        viewModel.updateNoApiKeyRequired(enabled: enabled)
    }
    
    func updateHeadrs(_ headers: [InstanceHeader]) {
        viewModel.updateHeaders(headers: headers)
    }

    func updateLocalNetworkEnabled(_ enabled: Bool) {
        viewModel.updateLocalNetworkEnabled(enabled: enabled)
    }

    func updateLocalNetworkUrl(_ url: String) {
        viewModel.updateLocalNetworkUrl(url: url)
    }

    func updateLocalNetworkSsids(_ ssids: [String]) {
        viewModel.updateLocalNetworkSsid(ssids: ssids)
    }

    func testConnection() {
        viewModel.testConnection()
    }

    func testLocalConnection() {
        viewModel.testLocalConnection()
    }
    
    func deleteClient() {
        viewModel.deleteClient()
    }
    
    func submit() {
        viewModel.submit()
    }

    func resetMutationState() {
        viewModel.resetMutationState()
    }
}
