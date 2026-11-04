//
//  UnifiedSearchViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-17.
//

import Shared
import SwiftUI

@MainActor
class UnifiedSearchViewModelS: ObservableObject {
    private let viewModel: UnifiedSearchViewModel

    @Published private(set) var searchState: [SearchResult] = []
    @Published private(set) var filteredSearchState: [SearchResult] = []
    @Published private(set) var availableFilters: [SearchFilter] = []
    @Published private(set) var selectedFilter: SearchFilter? = nil
    @Published private(set) var isSearching: Bool = false
    @Published private(set) var searchShowBanners: Bool = true
    @Published var searchQuery: String = ""

    init() {
        self.viewModel = KoinBridge.shared.getUnifiedSearchViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.searchState.observeAsync(on: self, to: \.searchState)
        viewModel.filteredSearchState.observeAsync(on: self, to: \.filteredSearchState)
        viewModel.availableFilters.observeAsync(on: self, to: \.availableFilters)
        viewModel.selectedFilter.observeAsync(on: self, to: \.selectedFilter)
        viewModel.isSearching.observeAsync(on: self) { owner, searching in
            owner.isSearching = searching.boolValue
        }
        viewModel.searchShowBanners.observeAsync(on: self) { owner, show in
            owner.searchShowBanners = show.boolValue
        }
    }

    func selectFilter(_ filter: SearchFilter?) {
        viewModel.selectFilter(filter: filter)
    }

    func updateSearchQuery(_ query: String) {
        self.searchQuery = query
        viewModel.updateSearchQuery(query: query)
    }

    func clearSearch() {
        self.searchQuery = ""
        viewModel.clearSearch()
    }
}
