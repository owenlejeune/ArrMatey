//
//  ProwlarrIndexersViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-21.
//

import Shared
import SwiftUI

@MainActor
class ProwlarrIndexersViewModelS: ObservableObject {
    private let viewModel: ProwlarrIndexersViewModel
    
    @Published private(set) var indexers: ProwlarrIndexersState = ProwlarrIndexersStateInitial()
    @Published private(set) var indexerSortState: IndexersSortingState = IndexersSortingState()
    @Published private(set) var indexersStatus: [IndexerStatus] = []
    
    init() {
        self.viewModel = KoinBridge.shared.getProwlarrIndexersViewModel()
        
        viewModel.indexers.observeAsync { self.indexers = $0 }
        viewModel.indexerSortState.observeAsync { self.indexerSortState = $0 }
        viewModel.indexerStatus.observeAsync { self.indexersStatus = $0 }
    }
    
    func refresh() {
        viewModel.refresh()
    }
    
    func updateSortOrder(_ sortOrder: Shared.SortOrder) {
        viewModel.updateSortOrder(sortOrder: sortOrder)
    }
    
    func updateSortBy(_ sortBy: SortBy) {
        viewModel.updateSortBy(sortBy: sortBy)
    }
}
