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
        viewModel.uiState.observeAsync { self.uiState = $0 }
    }
    
    func refreshHistory() {
        viewModel.refreshHistory()
    }
}
