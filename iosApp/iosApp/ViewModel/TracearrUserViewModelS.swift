import Shared
import SwiftUI

@MainActor
class TracearrUserViewModelS: ObservableObject {
    private let viewModel: TracearrUserViewModel

    @Published private(set) var state: TracearrUserState = TracearrUserStateInitial()
    @Published private(set) var selectedSession: TracearrStreamSession? = nil
    @Published private(set) var isRefreshing: Bool = false

    init(userRef: String) {
        self.viewModel = KoinBridge.shared.getTracearrUserViewModel(userRef: userRef)
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

    func loadMoreHistory() {
        viewModel.loadMoreHistory()
    }

    func setSelectedHistoryStream(_ historyItem: TracearrHistoryItem) {
        viewModel.setSelectedHistoryStream(historyItem: historyItem)
    }

    func clearSelected() {
        viewModel.clearSelected()
    }
}
