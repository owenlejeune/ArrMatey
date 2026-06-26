//
//  BazarrViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class BazarrViewModelS: ObservableObject {
    private let viewModel: BazarrViewModel

    @Published private(set) var wantedEpisodesState = PagedData<WantedEpisode>()
    @Published private(set) var wantedMoviesState = PagedData<WantedMovie>()
    @Published private(set) var providersState = ProvidersUiState(isLoading: false, providers: [], error: nil)

    init() {
        self.viewModel = KoinBridge.shared.getBazarrViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.wantedEpisodesState.observeAsync(on: self, to: \.wantedEpisodesState)
        viewModel.wantedMoviesState.observeAsync(on: self, to: \.wantedMoviesState)
        viewModel.providersState.observeAsync(on: self, to: \.providersState)
    }

    func refresh() {
        viewModel.refresh()
    }

    func loadMoreEpisodes() {
        viewModel.loadMoreEpisodes()
    }

    func loadMoreMovies() {
        viewModel.loadMoreMovies()
    }

    func loadProviders() {
        viewModel.loadProviders()
    }

    func resetProviders() {
        viewModel.resetProviders()
    }
}
