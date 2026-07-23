//
//  ArrMediaViewModelWrapper.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-16.
//

import Shared
import SwiftUI
import Combine

@MainActor
class ArrMediaViewModelS: ObservableObject {
    private let viewModel: ArrMediaViewModel
    
    @Published private(set) var uiState: ArrLibrary = ArrLibraryInitial()
    @Published private(set) var instanceData: InstanceData?
    @Published private(set) var addItemStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var editItemStatus: OperationStatus = OperationStatusIdle()
    @Published private(set) var preferences: InstancePreferences = InstancePreferences()
    @Published private(set) var hasServerConnectivityError: Bool = false
    @Published private(set) var errorMessage: String? = nil
    
    @Published var searchQuery: String = "" {
        didSet {
            updateSearchQuery(searchQuery)
        }
    }
    
    @Published private(set) var hasBazarr: Bool = false
    @Published private(set) var isInSelectionMode: Bool = false
    @Published private(set) var selectionCount: Int32 = 0
    @Published private(set) var selectedItems: Set<Int64> = []
    @Published private(set) var selectedItem: ArrMedia? = nil

    private var cancellables = Set<AnyCancellable>()
    
    init(type: InstanceType) {
        self.viewModel = KoinBridge.shared.getArrMediaViewModel(type: type)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.uiState.observeAsync(on: self, to: \.uiState)
        viewModel.instanceData.observeAsync(on: self, to: \.instanceData)
        viewModel.addItemStatus.observeAsync(on: self, to: \.addItemStatus)
        viewModel.editItemStatus.observeAsync(on: self, to: \.editItemStatus)
        viewModel.searchQuery.observeAsync(on: self, to: \.searchQuery)
        viewModel.preferences.observeAsync(on: self, to: \.preferences)
        viewModel.hasServerConnectivityError.observeAsync(on: self) { owner, error in
            owner.hasServerConnectivityError = error.boolValue
        }
        viewModel.errorMessage.observeAsync(on: self, to: \.errorMessage)
        viewModel.hasBazarr.observeAsync(on: self) { owner, hasBazarr in
            owner.hasBazarr = hasBazarr.boolValue
        }
        viewModel.selectionState.isInSelectionMode.observeAsync(on: self) { owner, isInSelectionMode in
            owner.isInSelectionMode = isInSelectionMode.boolValue
        }
        viewModel.selectionState.selectionCount.observeAsync(on: self) { owner, selectionCount in
            owner.selectionCount = selectionCount.int32Value
        }
        viewModel.selectionState.selectedItems.observeAsync(on: self) { owner, selectedItems in
            owner.selectedItems = Set(selectedItems.compactMap { ($0 as? NSNumber)?.int64Value })
        }
        viewModel.selectedItem.observeAsync(on: self, to: \.selectedItem)
    }
    
    func executeAutomaticSearch(_ seriesId: Int64) {
        viewModel.executeAutomaticSearch(seriesId: seriesId)
    }
    
    func updateViewType(_ type: ViewType) {
        viewModel.updateViewType(viewType: type)
    }
    
    func updateSortBy(_ sortBy: SortBy) {
        viewModel.updateSortBy(sortBy: sortBy)
    }
    
    func updateSortOrder(_ order: Shared.SortOrder) {
        viewModel.updateSortOrder(sortOrder: order)
    }
    
    func updateFilterBy(_ filterBy: FilterBy) {
        viewModel.updateFilterBy(filterBy: filterBy)
    }
    
    func updateCustomFilter(_ id: Int64?) {
        viewModel.updateCustomFilter(customFilterId: id?.asKotlinLong)
    }

    func updateShowFullDetails(_ show: Bool) {
        viewModel.updateShowFullDetails(show: show)
    }

    func updateShowOverlay(_ show: Bool) {
        viewModel.updateShowOverlay(show: show)
    }

    func updateShowBannerBackground(_ show: Bool) {
        viewModel.updateShowBannerBackground(show: show)
    }

    func updateIncludeOverview(_ show: Bool) {
        viewModel.updateIncludeOverview(show: show)
    }

    func updateBannerBlur(_ blur: Blur) {
        viewModel.updateBannerBlur(blur: blur)
    }

    func updateGridDensity(_ density: GridDensity) {
        viewModel.updateGridDensity(density: density)
    }

    func updateGridSpacing(_ spacing: GridSpacing) {
        viewModel.updateGridSpacing(spacing: spacing)
    }

    func updatePosterElevation(_ elevation: PosterElevation) {
        viewModel.updatePosterElevation(elevation: elevation)
    }

    func updatePosterRadius(_ radius: PosterRadius) {
        viewModel.updatePosterRadius(radius: radius)
    }

    func updateApplyGlobally(_ applyGlobally: Bool) {
        viewModel.updateApplyGlobally(applyGlobally: applyGlobally)
    }
    
    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }
    
    func refresh() {
        viewModel.refresh()
    }
    
    func toggleMonitored(_ item: ArrMedia) {
        viewModel.toggleMonitored(item: item)
    }
    
    func performAutomaticLookup(_ item: ArrMedia) {
        viewModel.performAutomaticLookup(item: item)
    }
    
    func performRefresh(_ item: ArrMedia) {
        viewModel.performRefresh(item: item)
    }
    
    func deleteMedia(_ item: ArrMedia, deleteFiles: Bool, addImportExclusion: Bool) {
        viewModel.deleteMedia(item: item, deleteFiles: deleteFiles, addImportExclusion: addImportExclusion)
    }
    
    func editItem(_ item: ArrMedia, moveFiles: Bool = false) {
        viewModel.editItem(item: item, moveFiles: moveFiles)
    }
    
    func toggleItemSelection(_ id: Int64) {
        viewModel.toggleItemSelection(id: id)
    }
    
    func selectAllItems() {
        viewModel.selectAllItems()
    }
    
    func clearSelection() {
        viewModel.clearSelection()
    }
    
    func exitSelectionMode() {
        viewModel.exitSelectionMode()
    }
    
    func enterSelectionMode() {
        viewModel.enterSelectionMode()
    }
    
    func areAllItemsSelected() -> Bool {
        return viewModel.areAllItemsSelected()
    }
    
    func refreshSelected() {
        viewModel.refreshSelected()
    }
    
    func deleteSelected(deleteFiles: Bool, addExclusion: Bool) {
        viewModel.deleteSelected(deleteFiles: deleteFiles, addExclusion: addExclusion)
    }
    
    func toggleMonitoringForSelected() {
        viewModel.toggleMonitoringForSelected()
    }
    
    func performAutomaticLookupSelected() {
        viewModel.performAutomaticLookupSelected()
    }
    
    func performSubtitleSearch(item: ArrMedia) {
        viewModel.performSubtitleSearch(item: item)
    }
    
    func performSubtitleSearchSelected() {
        viewModel.performSubtitleSearchSelected()
    }
    
    func updateMonitoringSelected(_ monitorType: Any) {
        viewModel.updateMonitoringSelected(monitorType: monitorType)
    }
}
