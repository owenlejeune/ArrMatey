//
//  UnifiedLibraryViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-08-21.
//

import Shared
import SwiftUI

@MainActor
class UnifiedLibraryViewModelS: ObservableObject {
    private let viewModel: UnifiedLibraryViewModel
    
    @Published private(set) var arrInstances: [Instance] = []
    @Published private(set) var selectedInstance: Instance? = nil
    @Published private(set) var offlineInstanceIds: Set<Int64> = []
    @Published private(set) var currentLibraryState: ArrLibrary = ArrLibraryInitial()
    @Published private(set) var instanceData: InstanceData?
    @Published private(set) var preferences: InstancePreferences = InstancePreferences()
    @Published private(set) var hasBazarr: Bool = false
    @Published private(set) var deleteStatus: NetworkingOperationStatus = NetworkingOperationStatusIdle()
    @Published private(set) var editItemStatus: NetworkingOperationStatus = NetworkingOperationStatusIdle()
    @Published private(set) var errorMessage: String? = nil
    @Published private(set) var lastSearchResult: Bool? = nil
    
    @Published var searchQuery: String = "" {
        didSet {
            viewModel.updateSearchQuery(query: searchQuery)
        }
    }
    
    @Published private(set) var isInSelectionMode: Bool = false
    @Published private(set) var selectionCount: Int32 = 0
    @Published private(set) var selectedItems: Set<Int64> = []
    @Published private(set) var selectedItem: ArrMedia? = nil
    @Published private(set) var activeMediaIdsByInstance: [Int64: Set<Int64>] = [:]
    
    init() {
        let vm = KoinBridge.shared.getUnifiedLibraryViewModel()
        self.viewModel = vm
        self.arrInstances = vm.arrInstances.value
        self.selectedInstance = vm.selectedInstance.value
        self.offlineInstanceIds = Set(vm.offlineInstanceIds.value.compactMap { ($0 as? NSNumber)?.int64Value })
        self.currentLibraryState = vm.currentLibraryState.value
        self.instanceData = vm.instanceData.value
        self.preferences = vm.preferences.value
        self.hasBazarr = vm.hasBazarr.value.boolValue
        self.deleteStatus = vm.deleteStatus.value
        self.editItemStatus = vm.editItemStatus.value
        self.errorMessage = vm.errorMessage.value
        self.isInSelectionMode = vm.selectionState.isInSelectionMode.value.boolValue
        self.selectionCount = vm.selectionState.selectionCount.value.int32Value
        self.selectedItems = Set(vm.selectionState.selectedItems.value.compactMap { ($0 as? NSNumber)?.int64Value })
        self.selectedItem = vm.selectedItem.value
        self.activeMediaIdsByInstance = vm.activeMediaIdsByInstance.value.reduce(into: [Int64: Set<Int64>]()) { result, entry in
            let key = entry.key.int64Value
            let values = Set(entry.value.compactMap { ($0 as? NSNumber)?.int64Value })
            result[key] = values
        }
        startObserving()
    }
    
    private func startObserving() {
        viewModel.arrInstances.observeAsync(on: self) { owner, instances in
            owner.arrInstances = instances
        }
        viewModel.selectedInstance.observeAsync(on: self) { owner, selected in
            owner.selectedInstance = selected
        }
        viewModel.offlineInstanceIds.observeAsync(on: self) { owner, offlineInstanceIds in
            owner.offlineInstanceIds = Set(offlineInstanceIds.compactMap { ($0 as? NSNumber)?.int64Value })
        }
        viewModel.currentLibraryState.observeAsync(on: self) { owner, state in
            owner.currentLibraryState = state
        }
        viewModel.instanceData.observeAsync(on: self) { owner, data in
            owner.instanceData = data
        }
        viewModel.preferences.observeAsync(on: self) { owner, prefs in
            owner.preferences = prefs
        }
        viewModel.activeMediaIdsByInstance.observeAsync(on: self) { owner, map in
            owner.activeMediaIdsByInstance = map.reduce(into: [Int64: Set<Int64>]()) { result, entry in
                let key = entry.key.int64Value
                let values = Set(entry.value.compactMap { ($0 as? NSNumber)?.int64Value })
                result[key] = values
            }
        }
        viewModel.hasBazarr.observeAsync(on: self) { owner, hasBazarr in
            owner.hasBazarr = hasBazarr.boolValue
        }
        viewModel.deleteStatus.observeAsync(on: self) { owner, status in
            owner.deleteStatus = status
        }
        viewModel.editItemStatus.observeAsync(on: self) { owner, status in
            owner.editItemStatus = status
        }
        viewModel.errorMessage.observeAsync(on: self) { owner, message in
            owner.errorMessage = message
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
        viewModel.selectedItem.observeAsync(on: self) { owner, item in
            owner.selectedItem = item
        }
    }
    
    func selectInstance(_ instance: Instance) {
        viewModel.selectInstance(instance: instance)
    }
    
    func isInstanceOffline(_ instanceId: Int64) -> Bool {
        offlineInstanceIds.contains(instanceId)
    }

    func isItemActive(instanceId: Int64, mediaId: Int64) -> Bool {
        activeMediaIdsByInstance[instanceId]?.contains(mediaId) ?? false
    }
    
    func refreshSelected() {
        viewModel.refreshSelected()
    }
    
    func updateFilterBy(_ filterBy: FilterBy) {
        viewModel.updateFilterBy(filterBy: filterBy)
    }
    
    func updateCustomFilter(_ customFilterId: Int64?) {
        viewModel.updateCustomFilter(customFilterId: customFilterId?.asKotlinLong)
    }
    
    func updateSortBy(_ sortBy: SortBy) {
        viewModel.updateSortBy(sortBy: sortBy)
    }
    
    func updateSortOrder(_ sortOrder: Shared.SortOrder) {
        viewModel.updateSortOrder(sortOrder: sortOrder)
    }
    
    func updateViewType(_ type: ViewType) {
        viewModel.updateViewType(viewType: type)
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
    
    func toggleItemSelection(_ id: Int64) {
        viewModel.toggleItemSelection(id: id)
    }
    
    func enterSelectionMode() {
        viewModel.enterSelectionMode()
    }
    
    func toggleMonitored(_ item: ArrMedia) {
        viewModel.toggleMonitored(item: item)
    }
    
    func performRefresh(_ item: ArrMedia) {
        viewModel.performRefresh(item: item)
    }
    
    func performAutomaticLookup(_ item: ArrMedia) {
        viewModel.performAutomaticLookup(item: item)
    }
    
    func performSubtitleSearch(_ item: ArrMedia) {
        viewModel.performSubtitleSearch(item: item)
    }
    
    func deleteMedia(_ item: ArrMedia, deleteFiles: Bool, addExclusion: Bool) {
        viewModel.deleteMedia(item: item, deleteFiles: deleteFiles, addImportExclusion: addExclusion)
    }
    
    func editItem(_ item: ArrMedia, moveFiles: Bool = false) {
        viewModel.editItem(item: item, moveFiles: moveFiles)
    }
    
    func selectAllItems() {
        viewModel.selectAllItems()
    }
    
    func areAllItemsSelected() -> Bool {
        viewModel.areAllItemsSelected()
    }
    
    func clearSelection() {
        viewModel.clearSelection()
    }
    
    func exitSelectionMode() {
        viewModel.exitSelectionMode()
    }
    
    func refreshSelectedItems() {
        viewModel.refreshSelectedItems()
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
    
    func performSubtitleSearchSelected() {
        viewModel.performSubtitleSearchSelected()
    }
    
    func runRssSync() {
        viewModel.runRssSync()
    }
    
    func searchAllMissing() {
        viewModel.searchAllMissing()
    }
    
    func updateLibrary() {
        viewModel.updateLibrary()
    }
    
    func backupDatabase() {
        viewModel.backupDatabase()
    }
}
