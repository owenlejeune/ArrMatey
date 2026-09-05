import Shared
import SwiftUI

@MainActor
class TracearrViewModelS: ObservableObject {
    private let viewModel: TracearrViewModel

    @Published private(set) var state: TracearrStreamsState = TracearrStreamsStateInitial()
    @Published private(set) var isRefreshing: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getTracearrViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.state.observeAsync(on: self, to: \.state)
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
}
