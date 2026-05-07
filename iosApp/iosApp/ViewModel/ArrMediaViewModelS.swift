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
    @Published private(set) var preferences: InstancePreferences = InstancePreferences()
    @Published private(set) var hasServerConnectivityError: Bool = false
    @Published private(set) var errorMessage: String? = nil
    
    @Published var searchQuery: String = "" {
        didSet {
            updateSearchQuery(searchQuery)
        }
    }
    
    private var cancellables = Set<AnyCancellable>()
    
    init(type: InstanceType) {
        self.viewModel = KoinBridge.shared.getArrMediaViewModel(type: type)
        startObserving()
    }
    
    private func startObserving() {
        viewModel.uiState.observeAsync { self.uiState = $0 }
        viewModel.instanceData.observeAsync { self.instanceData = $0 }
        viewModel.addItemStatus.observeAsync { self.addItemStatus = $0 }
        viewModel.searchQuery.observeAsync {self.searchQuery = $0 }
        viewModel.preferences.observeAsync { self.preferences = $0 }
        viewModel.hasServerConnectivityError.observeAsync { self.hasServerConnectivityError = $0.boolValue }
        viewModel.errorMessage.observeAsync { self.errorMessage = $0 }
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
    
    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }
    
    func refresh() {
        viewModel.refresh()
    }
}
