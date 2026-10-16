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
    private let preferencesStore: PreferencesStore

    @Published private(set) var queueItems: [QueueItem] = []
    @Published private(set) var historyItems: [HistoryItem] = []
    @Published private(set) var downloadedItems: [DownloadedMediaItem] = []
    @Published private(set) var blocklistItems: [BlocklistItem] = []
    @Published private(set) var missingItems: [WantedMediaItem] = []
    @Published private(set) var cutoffUnmetItems: [WantedMediaItem] = []
    @Published private(set) var tasksWithIssues: Int = 0
    @Published private(set) var isPolling: Bool = false
    @Published private(set) var isHistoryLoading: Bool = false
    @Published private(set) var isDownloadedLoading: Bool = false
    @Published private(set) var isBlocklistLoading: Bool = false
    @Published private(set) var isMissingLoading: Bool = false
    @Published private(set) var hasMissingLoaded: Bool = false
    @Published private(set) var isCutoffLoading: Bool = false
    @Published private(set) var hasCutoffLoaded: Bool = false
    @Published private(set) var filesystemItems: [FilesystemItem] = []
    @Published private(set) var isFilesystemLoading: Bool = false
    @Published private(set) var filesystemError: String? = nil
    @Published private(set) var instances: [Instance] = []
    @Published private(set) var uiState: ActivityQueueUiState = ActivityQueueUiState()
    @Published private(set) var removeItemStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var removeInProgress: Bool = false
    @Published private(set) var removeSuccesss: Bool = false
    @Published private(set) var useColoredCards: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getActivityQueueViewModel()
        self.preferencesStore = KoinBridge.shared.getPreferencesStore()
        startObserving()
    }

    private func startObserving() {
        viewModel.queueItems.observeAsync(on: self, to: \.queueItems)
        viewModel.historyItems.observeAsync(on: self, to: \.historyItems)
        viewModel.downloadedItems.observeAsync(on: self, to: \.downloadedItems)
        viewModel.blocklistItems.observeAsync(on: self, to: \.blocklistItems)
        viewModel.missingItems.observeAsync(on: self, to: \.missingItems)
        viewModel.cutoffUnmetItems.observeAsync(on: self, to: \.cutoffUnmetItems)
        viewModel.tasksWithIssues.observeAsync(on: self) { owner, tasks in
            owner.tasksWithIssues = tasks.intValue
        }
        viewModel.isPolling.observeAsync(on: self) { owner, isPolling in
            owner.isPolling = isPolling.boolValue
        }
        viewModel.isHistoryLoading.observeAsync(on: self) { owner, isHistoryLoading in
            owner.isHistoryLoading = isHistoryLoading.boolValue
        }
        viewModel.isDownloadedLoading.observeAsync(on: self) { owner, isDownloadedLoading in
            owner.isDownloadedLoading = isDownloadedLoading.boolValue
        }
        viewModel.isBlocklistLoading.observeAsync(on: self) { owner, isBlocklistLoading in
            owner.isBlocklistLoading = isBlocklistLoading.boolValue
        }
        viewModel.isMissingLoading.observeAsync(on: self) { owner, isMissingLoading in
            owner.isMissingLoading = isMissingLoading.boolValue
        }
        viewModel.hasMissingLoaded.observeAsync(on: self) { owner, hasMissingLoaded in
            owner.hasMissingLoaded = hasMissingLoaded.boolValue
        }
        viewModel.isCutoffLoading.observeAsync(on: self) { owner, isCutoffLoading in
            owner.isCutoffLoading = isCutoffLoading.boolValue
        }
        viewModel.hasCutoffLoaded.observeAsync(on: self) { owner, hasCutoffLoaded in
            owner.hasCutoffLoaded = hasCutoffLoaded.boolValue
        }
        viewModel.filesystemItems.observeAsync(on: self, to: \.filesystemItems)
        viewModel.isFilesystemLoading.observeAsync(on: self) { owner, isFilesystemLoading in
            owner.isFilesystemLoading = isFilesystemLoading.boolValue
        }
        viewModel.filesystemError.observeAsync(on: self, to: \.filesystemError)
        viewModel.instances.observeAsync(on: self, to: \.instances)
        viewModel.activityQueueUiState.observeAsync(on: self, to: \.uiState)
        viewModel.removeItemState.observeAsync(on: self) { owner, status in
            owner.removeItemStatus = status
            owner.removeInProgress = status is OperationStatusInProgress
            owner.removeSuccesss = status is OperationStatusSuccess
        }
        preferencesStore.useColoredActivityCards.observeAsync(on: self) { owner, val in
            owner.useColoredCards = val.boolValue
        }
    }

    func startPolling() {
        viewModel.startPolling()
    }

    func stopPolling() {
        viewModel.stopPolling()
    }

    func setSelectedTab(_ tab: ActivityTabSegment) {
        viewModel.setSelectedTab(tab: tab)
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

    func setHistoryStateFilter(_ filter: HistoryStateFilter) {
        viewModel.setHistoryStateFilter(filter: filter)
    }

    func setHistoryInstanceId(_ id: Int64?) {
        viewModel.setHistoryInstanceId(id: id?.asKotlinLong)
    }

    func setDownloadedInstanceId(_ id: Int64?) {
        viewModel.setDownloadedInstanceId(id: id?.asKotlinLong)
    }

    func setBlocklistInstanceId(_ id: Int64?) {
        viewModel.setBlocklistInstanceId(id: id?.asKotlinLong)
    }

    func setMissingInstanceId(_ id: Int64?) {
        viewModel.setMissingInstanceId(id: id?.asKotlinLong)
    }

    func setCutoffInstanceId(_ id: Int64?) {
        viewModel.setCutoffInstanceId(id: id?.asKotlinLong)
    }

    func setSearchQuery(_ query: String) {
        viewModel.setSearchQuery(query: query)
    }

    func deleteBlocklistItem(_ item: BlocklistItem) {
        viewModel.deleteBlocklistItem(item: item)
    }

    func refreshMissing() {
        viewModel.refreshMissing()
    }

    func refreshCutoff() {
        viewModel.refreshCutoff()
    }

    func searchAllMissing() {
        viewModel.searchAllMissing()
    }

    func executeAutomaticSearch(item: WantedMediaItem) {
        viewModel.executeAutomaticSearch(item: item)
    }

    func loadFilesystem(instanceId: Int64, path: String?) {
        viewModel.loadFilesystem(instanceId: instanceId, path: path)
    }

    func performManualImportForFolder(
        instanceId: Int64,
        folderPath: String,
        filterExisting: Bool = true,
        importMode: String = ManualImportMode.move.value,
        onSuccess: @escaping () -> Void,
        onError: @escaping (String) -> Void
    ) {
        viewModel.performManualImportForFolder(
            instanceId: instanceId,
            folderPath: folderPath,
            filterExisting: filterExisting,
            importMode: importMode,
            onSuccess: onSuccess,
            onError: onError
        )
    }

    func scanManualImportFolder(
        instanceId: Int64,
        folderPath: String,
        filterExisting: Bool = true,
        onSuccess: @escaping ([ManualImportFile]) -> Void,
        onError: @escaping (String) -> Void
    ) {
        viewModel.scanManualImportFolder(
            instanceId: instanceId,
            folderPath: folderPath,
            filterExisting: filterExisting,
            onSuccess: onSuccess,
            onError: onError
        )
    }

    func executeManualImport(
        instanceId: Int64,
        files: [ManualImportFile],
        importMode: String = ManualImportMode.move.value,
        onSuccess: @escaping () -> Void,
        onError: @escaping (String) -> Void
    ) {
        viewModel.executeManualImport(
            instanceId: instanceId,
            files: files,
            importMode: importMode,
            onSuccess: onSuccess,
            onError: onError
        )
    }

    func getMediaForInstance(
        instanceId: Int64,
        onSuccess: @escaping ([ArrMedia]) -> Void,
        onError: @escaping (String) -> Void
    ) {
        viewModel.getMediaForInstance(
            instanceId: instanceId,
            onSuccess: onSuccess,
            onError: onError
        )
    }

    func getEpisodesForSeries(
        instanceId: Int64,
        seriesId: Int64,
        seasonNumber: Int32? = nil,
        onSuccess: @escaping ([Episode]) -> Void,
        onError: @escaping (String) -> Void
    ) {
        let seasonKotlinInt = seasonNumber != nil ? KotlinInt(int: seasonNumber!) : nil
        viewModel.getEpisodesForSeries(
            instanceId: instanceId,
            seriesId: seriesId,
            seasonNumber: seasonKotlinInt,
            onSuccess: onSuccess,
            onError: onError
        )
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
