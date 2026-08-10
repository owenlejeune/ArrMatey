//
//  TrendingViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class TrendingViewModelS: ObservableObject {
    private let viewModel: TrendingViewModel
    
    @Published private(set) var trendingState = PagedData<DiscoverResult>()
    @Published private(set) var isRefreshing: Bool = false
    
    init() {
        self.viewModel = KoinBridge.shared.getTrendingViewModel()
        startObserving()
    }
    
    private func startObserving() {
        viewModel.trendingState.observeAsync(on: self, to: \.trendingState)
        viewModel.isRefreshing.observeAsync(on: self) { owner, refreshing in
            owner.isRefreshing = refreshing.boolValue
        }
    }
    
    func loadNextPage() {
        viewModel.loadNextPage()
    }
    
    func refresh() {
        viewModel.refresh()
    }
}
