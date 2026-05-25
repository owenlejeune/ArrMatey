//
//  CustomWebpageConfigurationViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-23.
//

import Shared
import SwiftUI

@MainActor
class CustomWebpageConfigurationViewModelS: ObservableObject {
    private let viewModel: CustomWebpageConfigurationViewModel
    
    @Published private(set) var webpages: [CustomWebpage] = []
    @Published private(set) var uiState: CustomWebpageUiState = CustomWebpageUiState()
    
    @Published private(set) var mutationSuccess: Bool = false
    
    init(webpageId: Int64?) {
        self.viewModel = KoinBridge.shared.getCustomWebpageConfigurationViewModel(webpageId: webpageId?.asKotlinLong)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.webpages.observeAsync(on: self, to: \.webpages)
        viewModel.uiState.observeAsync(on: self) { owner, state in
            owner.uiState = state
            owner.mutationSuccess = state.saveResult is InsertResultSuccess
        }
    }
    
    func setName(_ name: String) {
        viewModel.setName(name: name)
    }
    
    func setUrl(_ url: String) {
        viewModel.setUrl(url: url)
    }
    
    func setHeaders(_ headers: [InstanceHeader]) {
        viewModel.setHeaders(headers: headers)
    }
    
    func saveWebpage() {
        viewModel.saveWebpage()
    }
    
    func deleteWebpage() {
        viewModel.deleteWebpage()
    }
    
    func clearError() {
        viewModel.clearError()
    }
    
    func reset() {
        viewModel.reset()
    }
}
