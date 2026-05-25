//
//  MovieFilesViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-19.
//

import Shared
import SwiftUI

@MainActor
class MovieFilesViewModelS: ObservableObject {
    private let viewModel: MovieFilesViewModel
    
    @Published private(set) var uiState: MovieFilesState
    
    init(movieId: Int64) {
        self.viewModel = KoinBridge.shared.getMovieFilesViewModel(movieId: movieId)
        self.uiState = MovieFilesState()
        startObserving()
    }
    
    private func startObserving() {
        viewModel.uiState.observeAsync(on: self, to: \.uiState)
    }
    
    func refreshHistory() {
        viewModel.refreshHistory()
    }
}
