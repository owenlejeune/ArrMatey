import Shared
import SwiftUI

@MainActor
class TracearrUsersViewModelS: ObservableObject {
    private let viewModel: TracearrUsersViewModel

    @Published private(set) var state: TracearrUsersState = TracearrUsersState.Initial()
    @Published private(set) var isRefreshing: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getTracearrUsersViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.state.observeAsync(on: self, to: \.state)
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
}
