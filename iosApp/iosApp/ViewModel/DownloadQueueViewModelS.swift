//
//  DownloadQueueViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class DownloadQueueViewModelS: ObservableObject {
    private let viewModel: DownloadQueueViewModel

    @Published private(set) var clientIdsFilters: [Int64] = []
    @Published private(set) var sortState: DownloadQueueSortState = DownloadQueueSortState()
    @Published private(set) var downloadQueueState: DownloadQueueBundle = DownloadQueueBundle()
    @Published private(set) var commandState: DownloadClientCommandState = DownloadClientCommandStateInitial()
    @Published private(set) var isCommandLoading: Bool = false
    @Published private(set) var isCommandSuccess: Bool = false
    @Published private(set) var isRefreshing: Bool = false

    init() {
        self.viewModel = KoinBridge.shared.getDownloadQueueViewModel()
        startObserving()
    }

    private func startObserving() {
        viewModel.clientIdsFilters.observeAsync { self.clientIdsFilters = $0.map { $0.int64Value } }
        viewModel.sortState.observeAsync { self.sortState = $0 }
        viewModel.downloadQueueState.observeAsync { self.downloadQueueState = $0 }
        viewModel.commandState.observeAsync {
            self.commandState = $0
            self.isCommandLoading = $0 is DownloadClientCommandStateLoading
            self.isCommandSuccess = $0 is DownloadClientCommandStateSuccess
        }
        viewModel.isRefreshing.observeAsync { self.isRefreshing = $0.boolValue }
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
    
    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }
    
    func toggleClientIdFilter(id: Int64) {
        viewModel.toggleClientIdFilter(id: id)
    }
    
    func updateSortBy(_ by: SortBy) {
        viewModel.updateSortBy(sortBy: by)
    }
    
    func updateSortOrder(_ order: Shared.SortOrder) {
        viewModel.updateSortOrder(sortOrder: order)
    }
}
