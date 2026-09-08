import Shared
import SwiftUI

@MainActor
class TracearrViewModelS: ObservableObject {
    private let viewModel: TracearrViewModel

    @Published private(set) var state: TracearrState = TracearrStateInitial()
    @Published private(set) var selectedSession: TracearrStreamSession? = nil
    @Published private(set) var isRefreshing: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getTracearrViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.state.observeAsync(on: self, to: \.state)
        viewModel.selectedSession.observeAsync(on: self, to: \.selectedSession)
        viewModel.isRefreshing.observeAsync(on: self) { owner, refreshing in
            owner.isRefreshing = refreshing.boolValue
        }
    }

    func loadStreams() {
        viewModel.loadStreams()
    }

    func refresh() {
        viewModel.refresh()
    }

    func setSelectedStream(_ stream: TracearrStreamSession) {
        viewModel.setSelectedStream(streamSession: stream)
    }

    func setSelectedHistoryStream(_ historyItem: TracearrHistoryItem) {
        viewModel.setSelectedHistoryStream(historyItem: historyItem)
    }

    func clearSelected() {
        viewModel.clearSelected()
    }
}
