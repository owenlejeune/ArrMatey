import Shared
import SwiftUI

@MainActor
class TracearrUsersViewModelS: ObservableObject {
    private let viewModel: TracearrUsersViewModel

    @Published private(set) var state: TracearrUsersState = TracearrUsersState.Initial()
    @Published private(set) var isRefreshing: Bool = false
    @Published var searchQuery: String = "" {
        didSet {
            viewModel.updateSearchQuery(query: searchQuery)
        }
    }

    init() {
        self.viewModel = KoinBridge.shared.getTracearrUsersViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.state.observeAsync(on: self, to: \.state)
        viewModel.searchQuery.observeAsync(on: self) { owner, query in
            if owner.searchQuery != query {
                owner.searchQuery = query
            }
        }
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

    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }
}
