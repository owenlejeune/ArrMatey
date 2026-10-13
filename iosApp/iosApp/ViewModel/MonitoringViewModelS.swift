import Shared
import SwiftUI

@MainActor
class MonitoringViewModelS: ObservableObject {
    private let viewModel: MonitoringViewModel

    @Published private(set) var state: MonitoringState = MonitoringStateInitial()
    @Published private(set) var selectedSession: PlaybackStreamSession? = nil
    @Published private(set) var isRefreshing: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getMonitoringViewModel()
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

    func setSelectedStream(_ stream: PlaybackStreamSession) {
        viewModel.setSelectedStream(streamSession: stream)
    }

    func setSelectedHistoryStream(_ historyItem: PlaybackHistoryItem) {
        viewModel.setSelectedHistoryStream(historyItem: historyItem)
    }

    func terminateStream(_ session: PlaybackStreamSession, message: String? = nil) {
        viewModel.terminateStream(session: session, message: message)
    }

    func clearSelected() {
        viewModel.clearSelected()
    }
}
