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
        viewModel.uiState.observeAsync {
            self.uiState = $0
            self.mutationSuccess = $0.mutationState is DownloadClientMutationStateSuccess
        }
        viewModel.downloadClient.observeAsync { self.downloadClient = $0 }
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
    
    func updateEnabled(_ enabled: Bool) {
        viewModel.updateEnabled(enabled: enabled)
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
