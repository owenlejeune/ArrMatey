//
//  AudiobookFilesViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-19.
//

import Shared
import SwiftUI

@MainActor
class AudiobookFilesViewModelS: ObservableObject {
    private let viewModel: AudiobookFilesViewModel
    
    @Published private(set) var uiState: AudiobookFilesState = AudiobookFilesState()
    
    init(audiobookId: Int64) {
        self.viewModel = KoinBridge.shared.getAudiobookFilesViewModel(audiobookId: audiobookId)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.uiState.observeAsync(on: self, to: \.uiState)
    }
    
    func refreshHistory() {
        viewModel.refreshHistory()
    }
}
