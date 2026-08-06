//
//  DownloadQueueViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class DownloadQueueViewModelS: ObservableObject {
    private let viewModel: DownloadQueueViewModel

    @Published private(set) var filterState: DownloadQueueFilterState = DownloadQueueFilterState()
    @Published private(set) var sortState: DownloadQueueSortState = DownloadQueueSortState()
    @Published private(set) var downloadQueueState: DownloadQueueBundle = DownloadQueueBundle()
    @Published private(set) var commandState: DownloadClientCommandState = DownloadClientCommandStateInitial()
    @Published private(set) var isCommandLoading: Bool = false
    @Published private(set) var isCommandSuccess: Bool = false
    @Published private(set) var isCommandError: Bool = false
    @Published private(set) var isRefreshing: Bool = true
    @Published private(set) var hasLoaded: Bool = false
    @Published private(set) var errorMessage: String? = nil

    @Published private(set) var isInSelectionMode: Bool = false
    @Published private(set) var selectionCount: Int32 = 0
    @Published private(set) var selectedItems: Set<String> = []
    @Published private(set) var selectedItem: DownloadItem? = nil

    init() {
        self.viewModel = KoinBridge.shared.getDownloadQueueViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.filterState.observeAsync(on: self, to: \.filterState)
        viewModel.sortState.observeAsync(on: self, to: \.sortState)
        viewModel.downloadQueueState.observeAsync(on: self, to: \.downloadQueueState)
        viewModel.commandState.observeAsync(on: self) { owner, state in
            owner.commandState = state
            owner.isCommandLoading = state is DownloadClientCommandStateLoading
            owner.isCommandSuccess = state is DownloadClientCommandStateSuccess
            owner.isCommandError = state is DownloadClientCommandStateError
        }
        viewModel.isRefreshing.observeAsync(on: self) { owner, isRefreshing in
            owner.isRefreshing = isRefreshing.boolValue
        }
        viewModel.hasLoaded.observeAsync(on: self) { owner, hasLoaded in
            owner.hasLoaded = hasLoaded.boolValue
        }
        viewModel.errorMessage.observeAsync(on: self, to: \.errorMessage)

        viewModel.selectionState.isInSelectionMode.observeAsync(on: self) { owner, isInSelectionMode in
            owner.isInSelectionMode = isInSelectionMode.boolValue
        }
        viewModel.selectionState.selectionCount.observeAsync(on: self) { owner, count in
            owner.selectionCount = count.int32Value
        }
        viewModel.selectionState.selectedItems.observeAsync(on: self) { owner, items in
            owner.selectedItems = Set(items as? [String] ?? [])
        }
        viewModel.selectedItem.observeAsync(on: self, to: \.selectedItem)
    }
    
    func refresh() {
        viewModel.refresh()
    }

    func pauseDownload(_ id: String) {
        viewModel.pauseDownload(id: id)
    }

    func resumeDownload(_ id: String) {
        viewModel.resumeDownload(id: id)
    }

    func deleteDownload(_ id: String, deleteFiles: Bool) {
        viewModel.deleteDownload(id: id, deleteFiles: deleteFiles)
    }

    func resetCommandState() {
        viewModel.resetCommandState()
    }

    func toggleItemSelection(_ id: String) {
        viewModel.toggleItemSelection(id: id)
    }

    func selectAllItems() {
        viewModel.selectAllItems()
    }

    func clearSelection() {
        viewModel.clearSelection()
    }

    func enterSelectionMode() {
        viewModel.enterSelectionMode()
    }

    func exitSelectionMode() {
        viewModel.exitSelectionMode()
    }

    func areAllItemsSelected() -> Bool {
        return viewModel.areAllItemsSelected()
    }

    func pauseSelected() {
        viewModel.pauseSelected()
    }

    func resumeSelected() {
        viewModel.resumeSelected()
    }

    func deleteSelected(deleteFiles: Bool) {
        viewModel.deleteSelected(deleteFiles: deleteFiles)
    }
    
    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }
    
    func toggleClientIdFilter(id: Int64) {
        viewModel.toggleClientIdFilter(id: id)
    }

    func toggleStatusFilter(status: DownloadItemStatus) {
        viewModel.toggleStatusFilter(status: status)
    }

    func toggleTagFilter(tag: String) {
        viewModel.toggleTagFilter(tag: tag)
    }

    func updateActiveOnly(activeOnly: Bool) {
        viewModel.updateActiveOnly(activeOnly: activeOnly)
    }

    func updateCompletedOnly(completedOnly: Bool) {
        viewModel.updateCompletedOnly(completedOnly: completedOnly)
    }

    func updateExcludeTags(exclude: Bool) {
        viewModel.updateExcludeTags(exclude: exclude)
    }

    func updateExcludeStatuses(exclude: Bool) {
        viewModel.updateExcludeStatuses(exclude: exclude)
    }

    func clearFilters() {
        viewModel.clearFilters()
    }
    
    func updateSortBy(_ by: SortBy) {
        viewModel.updateSortBy(sortBy: by)
    }
    
    func updateSortOrder(_ order: Shared.SortOrder) {
        viewModel.updateSortOrder(sortOrder: order)
    }
}
