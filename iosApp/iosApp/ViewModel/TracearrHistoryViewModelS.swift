import Shared
import SwiftUI

@MainActor
class TracearrHistoryViewModelS: ObservableObject {
    private let viewModel: TracearrHistoryViewModel

    @Published private(set) var state: TracearrHistoryState = TracearrHistoryStateInitial()
    @Published private(set) var selectedSession: TracearrStreamSession? = nil
    @Published private(set) var isRefreshing: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getTracearrHistoryViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.state.observeAsync(on: self, to: \.state)
        viewModel.selectedSession.observeAsync(on: self, to: \.selectedSession)
        viewModel.isRefreshing.observeAsync(on: self) { owner, refreshing in
            owner.isRefreshing = refreshing.boolValue
        }
    }

    func refresh() {
        viewModel.refresh()
    }

    func loadMore() {
        viewModel.loadMore()
    }

    func setSelectedHistoryStream(_ historyItem: TracearrHistoryItem) {
        viewModel.setSelectedHistoryStream(historyItem: historyItem)
    }

    func clearSelected() {
        viewModel.clearSelected()
    }
}
