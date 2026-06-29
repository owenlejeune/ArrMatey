//
//  BazarrViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class BazarrViewModelS: ObservableObject {
    private let viewModel: BazarrViewModel

    @Published private(set) var uiState: BazarrLibrary = BazarrLibraryInitial()
    @Published private(set) var selectedSection: BazarrSection = BazarrSection.series
    @Published var searchQuery: String = ""

    init() {
        self.viewModel = KoinBridge.shared.getBazarrViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.uiState.observeAsync(on: self, to: \.uiState)
        viewModel.selectedSection.observeAsync(on: self, to: \.selectedSection)
        viewModel.searchQuery.observeAsync(on: self, to: \.searchQuery)
    }

    func selectSection(_ section: BazarrSection) {
        viewModel.selectSection(section: section)
    }

    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }

    func refresh() {
        viewModel.refresh()
    }

    func resetProviders() {
        viewModel.resetProviders()
    }
}
