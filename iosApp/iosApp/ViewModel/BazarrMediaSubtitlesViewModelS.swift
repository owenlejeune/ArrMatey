//
//  BazarrMediaSubtitlesViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class BazarrMediaSubtitlesViewModelS: ObservableObject {
    private let viewModel: BazarrMediaSubtitlesViewModel

    @Published private(set) var state: BazarrSubtitlesUiState = BazarrSubtitlesUiStateLoading()
    @Published private(set) var operationState: OperationStatus = OperationStatusIdle()

    init(target: BazarrMediaTarget) {
        self.viewModel = KoinBridge.shared.getBazarrMediaSubtitlesViewModel(target: target)
        startObserving()
    }

    private func startObserving() {
        viewModel.state.observeAsync(on: self, to: \.state)
        viewModel.operationState.observeAsync(on: self, to: \.operationState)
    }

    func load() {
        viewModel.load()
    }

    func autoSearch(_ language: BazarrSubtitleLanguage) {
        viewModel.autoSearch(language: language)
    }

    func delete(_ subtitle: BazarrSubtitle) {
        viewModel.delete(subtitle: subtitle)
    }

    func downloadToDevice(_ subtitle: BazarrSubtitle, completion: @escaping (Data?) -> Void) {
        viewModel.downloadToDevice(subtitle: subtitle) { bytes in
            if let bytes = bytes {
                completion(bytes.toData())
            } else {
                completion(nil)
            }
        }
    }
}
