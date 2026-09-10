import Shared
import SwiftUI

@MainActor
class TracearrActivityViewModelS: ObservableObject {
    private let viewModel: TracearrActivityViewModel

    @Published private(set) var state: TracearrActivityState = TracearrActivityStateInitial()
    @Published private(set) var selectedPeriod: TracearrPeriod = .month
    @Published private(set) var isRefreshing: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getTracearrActivityViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.state.observeAsync(on: self, to: \.state)
        viewModel.selectedPeriod.observeAsync(on: self, to: \.selectedPeriod)
        viewModel.isRefreshing.observeAsync(on: self) { owner, refreshing in
            owner.isRefreshing = refreshing.boolValue
        }
    }

    func setPeriod(_ period: TracearrPeriod) {
        viewModel.setPeriod(period: period)
    }

    func refresh() {
        viewModel.refresh()
    }
}
