//
//  MediaPreviewViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-19.
//

import Shared
import SwiftUI

@MainActor
class MediaPreviewViewModelS: ObservableObject {
    private let viewModel: MediaPreviewViewModel
    
    @Published private(set) var uiState: MediaPreviewUiState = MediaPreviewUiState()
    
    init(preview: ArrMedia, type: InstanceType) {
        self.viewModel = KoinBridge.shared.getMediaPreviewViewModel(preview: preview, type: type)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.uiState.observeAsync(on: self, to: \.uiState)
    }
    
    func addItem(_ item: ArrMedia, _ searchOnAdd: Bool) {
        viewModel.addItem(item: item, searchOnAdd: searchOnAdd)
    }
}
