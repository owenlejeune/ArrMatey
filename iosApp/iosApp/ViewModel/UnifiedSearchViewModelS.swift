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
    @Published private(set) var availableTypeFilters: [InstanceType] = []
    @Published private(set) var selectedTypeFilter: InstanceType? = nil
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
        viewModel.availableTypeFilters.observeAsync(on: self, to: \.availableTypeFilters)
        viewModel.selectedTypeFilter.observeAsync(on: self, to: \.selectedTypeFilter)
        viewModel.isSearching.observeAsync(on: self) { owner, searching in
            owner.isSearching = searching.boolValue
        }
        viewModel.searchShowBanners.observeAsync(on: self) { owner, show in
            owner.searchShowBanners = show.boolValue
        }
    }

    func selectTypeFilter(_ type: InstanceType?) {
        viewModel.selectTypeFilter(type: type)
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
