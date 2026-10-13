import Shared
import SwiftUI

@MainActor
class MonitoringHistoryViewModelS: ObservableObject {
    private let viewModel: MonitoringHistoryViewModel

    @Published private(set) var state: MonitoringHistoryState = MonitoringHistoryStateInitial()
    @Published private(set) var selectedSession: PlaybackStreamSession? = nil
    @Published private(set) var isRefreshing: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getMonitoringHistoryViewModel()
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

    func setSelectedHistoryStream(_ historyItem: PlaybackHistoryItem) {
        viewModel.setSelectedHistoryStream(historyItem: historyItem)
    }

    func clearSelected() {
        viewModel.clearSelected()
    }
}
