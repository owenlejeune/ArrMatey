//
//  InteractiveSearchViewModelS.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-19.
//

import Shared
import SwiftUI

@MainActor
class InteractiveSearchViewModelS: ObservableObject {
    private let viewModel: InteractiveSearchViewModel
    
    @Published private(set) var releaseUiState: ReleaseLibrary = ReleaseLibraryInitial()
    @Published private(set) var downloadReleaseState: DownloadState = DownloadStateInitial()
    @Published private(set) var filterUiState: InteractiveSearchUiState
    @Published private(set) var downloadStatus: Bool? = nil
    @Published var searchQuery: String = "" {
        didSet {
            updateSearchQuery(searchQuery)
        }
    }
    
    init(type: InstanceType, defaultFilter: ReleaseFilterBy) {
        self.filterUiState = InteractiveSearchUiState.companion.empty(filterBy: defaultFilter)
        self.viewModel = KoinBridge.shared.getInteractiveSearchViewModel(type: type, defaultFilter: defaultFilter)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.releaseUiState.observeAsync(on: self, to: \.releaseUiState)
        viewModel.downloadReleaseState.observeAsync(on: self, to: \.downloadReleaseState)
        viewModel.filterUiState.observeAsync(on: self, to: \.filterUiState)
        viewModel.downloadStatus.observeAsync(on: self) { owner, status in
            owner.downloadStatus = status?.boolValue
        }
        viewModel.searchQuery.observeAsync(on: self, to: \.searchQuery)
    }
    
    func getRelease(_ params: ReleaseParams) {
        viewModel.getRelease(params: params)
    }
    
    func downloadRelease(_ release: ArrRelease, _ force: Bool = false) {
        viewModel.downloadRelease(release: release, force: force)
    }
    
    func resetDownloadState() {
        viewModel.resetDownloadState()
    }
    
    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }
    
    func setSortOrder(_ sortOrder: Shared.SortOrder) {
        viewModel.setSortOrder(sortOrder: sortOrder)
    }
    
    func setSortBy(_ sortBy: ReleaseSortBy) {
        viewModel.setSortBy(sortBy: sortBy)
    }
    
    func setFilterby(_ filterBy: ReleaseFilterBy) {
        viewModel.setFilterBy(filterBy: filterBy)
    }
    
    func setFilterLanguage(_ filterLanguage: Shared.Language?) {
        viewModel.setFilterLanguage(language: filterLanguage)
    }
    
    func setFilterIndexer(_ indexer: String?) {
        viewModel.setFilterIndexer(indexer: indexer)
    }
    
    func setFilterQuality(_ quality: QualityInfo?) {
        viewModel.setFilterQuality(qualityInfo: quality)
    }
    
    func setFilterProtocol(_ filterProtocol: ReleaseProtocol?) {
        viewModel.setFilterProtocol(protocol: filterProtocol)
    }
    
    func setFilterCustomFormat(_ customFormat: CustomFormat?) {
        viewModel.setFilterCustomFormat(customFormat: customFormat)
    }
    
}
