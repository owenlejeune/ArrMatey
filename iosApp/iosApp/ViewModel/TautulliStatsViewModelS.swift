import Shared
import SwiftUI

@MainActor
class TautulliStatsViewModelS: ObservableObject {
    private let viewModel: TautulliStatsViewModel

    @Published private(set) var state: TautulliStatsState = TautulliStatsStateInitial()
    @Published private(set) var isRefreshing: Bool = false
    @Published private(set) var timeRange: Int32 = 30
    @Published private(set) var statsType: TautulliStatsType = .plays

    init() {
        self.viewModel = KoinBridge.shared.getTautulliStatsViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.state.observeAsync(on: self, to: \.state)
        viewModel.isRefreshing.observeAsync(on: self) { owner, refreshing in
            owner.isRefreshing = refreshing.boolValue
        }
        viewModel.timeRange.observeAsync(on: self) { owner, days in
            owner.timeRange = days.int32Value
        }
        viewModel.statsType.observeAsync(on: self) { owner, type in
            owner.statsType = type
        }
    }

    func refresh() {
        viewModel.refresh()
    }

    func setTimeRange(_ days: Int32) {
        viewModel.setTimeRange(days: days)
    }

    func setStatsType(_ type: TautulliStatsType) {
        viewModel.setStatsType(type: type)
    }
}
