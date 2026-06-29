//
//  BazarrDetailsViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class BazarrDetailsViewModelS: ObservableObject {
    private let viewModel: BazarrDetailsViewModel

    @Published private(set) var uiState: BazarrDetails = BazarrDetails(details: nil, episodes: [])
    @Published private(set) var operationState: OperationStatus = OperationStatusIdle()

    init(id: Int64, type: BazarrMediaType) {
        self.viewModel = KoinBridge.shared.getBazarrDetailsViewModel(id: id, type: type)
        startObserving()
    }

    private func startObserving() {
        viewModel.uiState.observeAsync(on: self, to: \.uiState)
        viewModel.operationState.observeAsync(on: self, to: \.operationState)
    }

    func performSearch() {
        viewModel.performSearch()
    }

    func clearOperation() {
        viewModel.clearOperation()
    }
}
