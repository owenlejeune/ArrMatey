import Shared
import SwiftUI

@MainActor
class TautulliUsersViewModelS: ObservableObject {
    private let viewModel: TautulliUsersViewModel

    @Published private(set) var state: TautulliUsersState = TautulliUsersState.Initial()
    @Published private(set) var isRefreshing: Bool = false
    @Published var searchQuery: String = ""

    init() {
        self.viewModel = KoinBridge.shared.getTautulliUsersViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.state.observeAsync(on: self, to: \.state)
        viewModel.isRefreshing.observeAsync(on: self) { owner, refreshing in
            owner.isRefreshing = refreshing.boolValue
        }
        viewModel.searchQuery.observeAsync(on: self) { owner, query in
            owner.searchQuery = query
        }
    }

    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }

    func refresh() {
        viewModel.refresh()
    }
}
