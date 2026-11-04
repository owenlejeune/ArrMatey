//
//  UnifiedSearchScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-09-17.
//

import SwiftUI
import Shared

struct UnifiedSearchScreen: View {
    @StateObject private var viewModel = UnifiedSearchViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager
    
    @State private var searchQuery: String
    @State private var searchPresented: Bool = false
    
    init(query: String = "") {
        self._searchQuery = State(initialValue: query)
    }
    
    private var itemCounts: [String: Int] {
        var counts: [String: Int] = [:]
        for filter in viewModel.availableFilters {
            counts[filter.id] = viewModel.searchState.filter { filter.matches(result: $0) }.count
        }
        return counts
    }
    
    var body: some View {
        VStack(spacing: 0) {
            SearchFilterGlassRow(
                selectedFilter: viewModel.selectedFilter,
                availableFilters: viewModel.availableFilters,
                itemCounts: itemCounts,
                totalCount: viewModel.searchState.count,
                onFilterSelected: { filter in
                    viewModel.selectFilter(filter)
                }
            )

            Group {
                if viewModel.isSearching && viewModel.searchState.isEmpty {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if !viewModel.searchState.isEmpty {
                    if viewModel.filteredSearchState.isEmpty && viewModel.selectedFilter != nil {
                        ContentUnavailableView {
                            Label("No \(viewModel.selectedFilter?.displayName ?? "") Results", systemImage: "line.3.horizontal.decrease.circle")
                        } description: {
                            Text("No results match the selected filter.")
                        } actions: {
                            Button(MR.strings().all.localized()) {
                                viewModel.selectFilter(nil)
                            }
                            .buttonStyle(.borderedProminent)
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        List {
                            ForEach(viewModel.filteredSearchState, id: \.id) { item in
                                DiscoverSearchResultRow(
                                    item: item,
                                    showBanners: viewModel.searchShowBanners,
                                    onItemClick: { result in
                                        handleItemClick(result)
                                    }
                                )
                                .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                                .listRowSeparator(.hidden)
                            }
                        }
                        .listStyle(.plain)
                    }
                } else if !searchQuery.isEmpty && !viewModel.isSearching {
                    ContentUnavailableView.search(text: searchQuery)
                } else {
                    ContentUnavailableView(
                        MR.strings().search.localized(),
                        systemImage: "magnifyingglass"
                    )
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .navigationTitle(MR.strings().search.localized())
        .navigationBarTitleDisplayMode(.inline)
        .task {
            try? await Task.sleep(nanoseconds: 500_000_000)
            searchPresented = true
        }
        .onDebounceSearch(searchQuery, initial: true) { query in
            if query.isEmpty {
                viewModel.clearSearch()
            } else {
                viewModel.updateSearchQuery(query)
            }
        }
        .searchable(text: $searchQuery, isPresented: $searchPresented, placement: .navigationBarDrawer(displayMode: .always))
    }
    
    private func handleItemClick(_ result: SearchResult) {
        if let arrResult = result as? SearchResultArrMediaResult {
            navigationManager.goToArrDetailsOrPreview(item: arrResult.media, type: arrResult.instanceType, instanceId: arrResult.instanceId?.int64Value)
        } else if let mediaServer = result as? SearchResultMediaServerResult {
            navigationManager.navigateToMediaServerItem(item: mediaServer.item)
        } else if let seerrMedia = result as? SearchResultSeerrMediaResult {
            navigationManager.goToSeerrDetails(tmdbId: seerrMedia.result.id, requestType: seerrMedia.result.mediaType)
        } else if let seerrPerson = result as? SearchResultSeerrPersonResult {
            navigationManager.goToPersonDetails(id: seerrPerson.result.id)
        }
    }
}
