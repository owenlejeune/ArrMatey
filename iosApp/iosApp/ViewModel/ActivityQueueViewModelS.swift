//
//  ActivityQueueViewModelWrapper.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-16.
//

import Shared
import SwiftUI

@MainActor
class ActivityQueueViewModelS: ObservableObject {
    private let viewModel: ActivityQueueViewModel
    
    @Published private(set) var queueItems: [QueueItem] = []
    @Published private(set) var tasksWithIssues: Int = 0
    @Published private(set) var isPolling: Bool = false
    @Published private(set) var instances: [Instance] = []
    @Published private(set) var uiState: ActivityQueueUiState = ActivityQueueUiState()
    @Published private(set) var removeItemStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var removeInProgress: Bool = false
    @Published private(set) var removeSuccesss: Bool = false
    
    init() {
        self.viewModel = KoinBridge.shared.getActivityQueueViewModel()
        startObserving()
    }
    
    private func startObserving() {
        viewModel.queueItems.observeAsync(on: self, to: \.queueItems)
        viewModel.tasksWithIssues.observeAsync(on: self) { owner, tasks in
            owner.tasksWithIssues = tasks.intValue
        }
        viewModel.isPolling.observeAsync(on: self) { owner, isPolling in
            owner.isPolling = isPolling.boolValue
        }
        viewModel.instances.observeAsync(on: self, to: \.instances)
        viewModel.activityQueueUiState.observeAsync(on: self, to: \.uiState)
        viewModel.removeItemState.observeAsync(on: self) { owner, status in
            owner.removeItemStatus = status
            owner.removeInProgress = status is OperationStatusInProgress
            owner.removeSuccesss = status is OperationStatusSuccess
        }
    }
    
    func startPolling() {
        viewModel.startPolling()
    }
    
    func stopPolling() {
        viewModel.stopPolling()
    }
    
    func setInstanceId(_ id: Int64?) {
        viewModel.setInstanceId(id: id?.asKotlinLong)
    }
    
    func setSortBy(_ sortBy: QueueSortBy) {
        viewModel.setSortBy(sortBy: sortBy)
    }
    
    func setSortOrder(_ order: Shared.SortOrder) {
        viewModel.setSortOrder(order: order)
    }
    
    func getQueueItemForEpisode(_ episode: Episode) -> SonarrQueueItem? {
        return viewModel.getQueueItemForEpisode(episode: episode)
    }
    
    func removeQueueItem(_ item: QueueItem, _ removeFromClient: Bool, _ addToBlocklist: Bool, _ skipRedownload: Bool) {
        viewModel.removeQueueItem(item: item, removeFromClient: removeFromClient, addToBlocklist: addToBlocklist, skipRedownload: skipRedownload)
    }
    
    func refresh() {
        viewModel.refresh()
    }
    
}
