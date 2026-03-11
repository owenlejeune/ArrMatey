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
        
        viewModel.searchResults.observeAsync { self.searchResults = $0 }
        viewModel.searchQuery.observeAsync { self.searchQuery = $0 }
        viewModel.grabStatus.observeAsync { self.grabStatus = $0 }
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
