//
//  BazarrSubtitleSearchViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class BazarrSubtitleSearchViewModelS: ObservableObject {
    private let viewModel: BazarrSubtitleSearchViewModel

    @Published private(set) var searchState: SubtitleSearchState = SubtitleSearchStateLoading()
    @Published private(set) var downloadStates: [String: OperationStatus] = [:]

    init(target: BazarrMediaTarget) {
        self.viewModel = KoinBridge.shared.getBazarrSubtitleSearchViewModel(target: target)
        startObserving()
    }

    private func startObserving() {
        viewModel.searchState.observeAsync(on: self, to: \.searchState)
        viewModel.downloadStates.observeAsync(on: self, to: \.downloadStates)
    }

    func search() {
        viewModel.search()
    }

    func download(_ result: ProviderSubtitle) {
        viewModel.download(result: result)
    }

    func downloadStatus(for result: ProviderSubtitle) -> OperationStatus? {
        downloadStates["\(result.provider):\(result.subtitle)"]
    }
}
