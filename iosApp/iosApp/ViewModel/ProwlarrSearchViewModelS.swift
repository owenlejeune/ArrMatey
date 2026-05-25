//
//  ProwlarrSearchViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-21.
//

import Shared
import SwiftUI

@MainActor
class ProwlarrSearchViewModelS: ObservableObject {
    private let viewModel: ProwlarrSearchViewModel
    
    @Published private(set) var searchResults: ProwlarrSearchState = ProwlarrSearchStateInitial()
    @Published private(set) var searchQuery: String = ""
    @Published private(set) var grabStatus: OperationStatus = OperationStatusIdle()
    
    init() {
        self.viewModel = KoinBridge.shared.getProwlarrSearchViewModel()
        
        viewModel.searchResults.observeAsync(on: self, to: \.searchResults)
        viewModel.searchQuery.observeAsync(on: self, to: \.searchQuery)
        viewModel.grabStatus.observeAsync(on: self, to: \.grabStatus)
    }
    
    func performSearch(_ query: String) {
        viewModel.performSearch(query: query)
    }
    
    func clearSearch() {
        viewModel.clearSearch()
    }
    
    func grabRelease(_ result: ProwlarrSearchResult) {
        viewModel.grabRelease(result: result)
    }
    
    func resetGrabStatus() {
        viewModel.resetGrabStatus()
    }
}
