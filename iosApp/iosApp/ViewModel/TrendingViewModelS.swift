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
    @Published private(set) var moviesState = PagedData<DiscoverResult>()
    @Published private(set) var tvState = PagedData<DiscoverResult>()
    @Published private(set) var isRefreshing: Bool = false
    
    init() {
        self.viewModel = KoinBridge.shared.getTrendingViewModel()
        startObserving()
    }
    
    private func startObserving() {
        viewModel.trendingState.observeAsync(on: self, to: \.trendingState)
        viewModel.moviesState.observeAsync(on: self, to: \.moviesState)
        viewModel.tvState.observeAsync(on: self, to: \.tvState)
        viewModel.isRefreshing.observeAsync(on: self) { owner, refreshing in
            owner.isRefreshing = refreshing.boolValue
        }
    }
    
    func loadNextTrendingPage() {
        viewModel.loadNextTrendingPage()
    }

    func loadNextMoviesPage() {
        viewModel.loadNextMoviesPage()
    }

    func loadNextTvPage() {
        viewModel.loadNextTvPage()
    }
    
    func refresh() {
        viewModel.refresh()
    }
}
