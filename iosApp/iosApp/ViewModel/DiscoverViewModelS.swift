//
//  TrendingViewModelS.swift
//  iosApp
//

import Shared
import SwiftUI

@MainActor
class DiscoverViewModelS: ObservableObject {
    private let viewModel: DiscoverViewModel
    
    @Published private(set) var trendingState = PagedData<DiscoverResult>()
    @Published private(set) var moviesState = PagedData<DiscoverResult>()
    @Published private(set) var tvState = PagedData<DiscoverResult>()
    @Published private(set) var upcomingMoviesState = PagedData<DiscoverResult>()
    @Published private(set) var upcomingTvState = PagedData<DiscoverResult>()
    @Published private(set) var searchResults: [SearchResult] = []
    @Published private(set) var isSearching: Bool = false
    @Published private(set) var isRefreshing: Bool = false
    @Published private(set) var searchShowBanners: Bool = true
    @Published private(set) var searchShowInstanceIndicatorShadow: Bool = true
    
    init() {
        self.viewModel = KoinBridge.shared.getDiscoverViewModel()
        startObserving()
    }
    
    private func startObserving() {
        viewModel.trendingState.observeAsync(on: self, to: \.trendingState)
        viewModel.moviesState.observeAsync(on: self, to: \.moviesState)
        viewModel.tvState.observeAsync(on: self, to: \.tvState)
        viewModel.upcomingMoviesState.observeAsync(on: self, to: \.upcomingMoviesState)
        viewModel.upcomingTvState.observeAsync(on: self, to: \.upcomingTvState)
        viewModel.searchState.observeAsync(on: self) { owner, results in
            owner.searchResults = results as? [SearchResult] ?? []
        }
        viewModel.isSearching.observeAsync(on: self) { owner, searching in
            owner.isSearching = searching.boolValue
        }
        viewModel.isRefreshing.observeAsync(on: self) { owner, refreshing in
            owner.isRefreshing = refreshing.boolValue
        }
        viewModel.searchShowBanners.observeAsync(on: self) { owner, show in
            owner.searchShowBanners = show.boolValue
        }
        viewModel.searchShowInstanceIndicatorShadow.observeAsync(on: self) { owner, show in
            owner.searchShowInstanceIndicatorShadow = show.boolValue
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

    func loadNextUpcomingMoviesPage() {
        viewModel.loadNextUpcomingMoviesPage()
    }

    func loadNextUpcomingTvPage() {
        viewModel.loadNextUpcomingTvPage()
    }
    
    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }

    func refresh() {
        viewModel.refresh()
    }
}
