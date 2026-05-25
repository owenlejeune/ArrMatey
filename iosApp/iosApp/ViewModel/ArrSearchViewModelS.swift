//
//  ArrSearchViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-19.
//

import Shared
import SwiftUI

@MainActor
class ArrSearchViewModelS: ObservableObject {
    private let viewModel: ArrSearchViewModel
    
    @Published private(set) var uiState: ArrLibrary = ArrLibraryInitial()
    @Published private(set) var sortBy: SortBy = .relevance
    @Published private(set) var sortOrder: Shared.SortOrder = .asc
    
    init(type: InstanceType) {
        self.viewModel = KoinBridge.shared.getArrSearchViewModel(type: type)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.lookupUiState.observeAsync(on: self, to: \.uiState)
        viewModel.sortBy.observeAsync(on: self, to: \.sortBy)
        viewModel.sortOrder.observeAsync(on: self, to: \.sortOrder)
    }
    
    func performLookup(_ query: String) {
        viewModel.performLookup(query: query)
    }
    
    func setSortBy(_ sortBy: SortBy) {
        viewModel.setSortBy(sortBy: sortBy)
    }
    
    func setSortOrder(_ sortOrder: Shared.SortOrder) {
        viewModel.setSortOrder(sortOrder: sortOrder)
    }
    
    func clearLookup() {
        viewModel.clearLookup()
    }
}
