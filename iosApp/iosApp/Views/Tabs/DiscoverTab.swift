//
//  DiscoverTab.swift
//  iosApp
//

import SwiftUI
import Shared

struct DiscoverTab: View {
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager

    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.seerrPath) { // Using seerrPath for now
                DiscoverTabContent()
                    .navigationDestination(for: SeerrRoute.self) { route in
                        SeerrRouteDestination(route: route)
                    }
            }
        case .launcher:
            DiscoverTabContent()
        }
    }
}

private struct DiscoverTabContent: View {
    @StateObject private var viewModel = DiscoverViewModelS()
    @StateObject private var instancesViewModel = InstancesViewModelS(type: .seerr)
    @EnvironmentObject private var navigationManager: NavigationManager
    @Environment(\.navigationContext) private var context
    @State private var searchQuery = ""
    @State private var showCustomizationSheet = false

    var body: some View {
        Group {
            if instancesViewModel.instancesState.selectedInstance == nil {
                NoInstanceView(type: .seerr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                Group {
                    if searchQuery.isEmpty {
                        if viewModel.isInitialLoading {
                            ProgressView()
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                        } else {
                            ScrollView {
                                VStack(alignment: .leading, spacing: 24) {
                                    ForEach(viewModel.discoverSectionPreferences.visibleCategories, id: \.self) { category in
                                        switch category {
                                        case .trending:
                                            DiscoverSection(
                                                title: MR.strings().trending.localized(),
                                                icon: "chart.line.uptrend.xyaxis",
                                                data: viewModel.trendingState,
                                                onItemClick: { item in
                                                    navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                                                },
                                                onItemClickArr: { result in
                                                    handleItemClick(result)
                                                },
                                                onLoadMore: { viewModel.loadNextTrendingPage() },
                                                onSeeMore: {
                                                    navigationManager.goToDiscoverCategory(category: .trending)
                                                }
                                            )
                                        case .popularMovies:
                                            DiscoverSection(
                                                title: MR.strings().popular_movies.localized(),
                                                icon: "movieclapper",
                                                data: viewModel.moviesState,
                                                onItemClick: { item in
                                                    navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                                                },
                                                onItemClickArr: { result in
                                                    handleItemClick(result)
                                                },
                                                onLoadMore: { viewModel.loadNextMoviesPage() },
                                                onSeeMore: {
                                                    navigationManager.goToDiscoverCategory(category: .popularMovies)
                                                }
                                            )
                                        case .popularSeries:
                                            DiscoverSection(
                                                title: MR.strings().popular_series.localized(),
                                                icon: "tv",
                                                data: viewModel.tvState,
                                                onItemClick: { item in
                                                    navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                                                },
                                                onItemClickArr: { result in
                                                    handleItemClick(result)
                                                },
                                                onLoadMore: { viewModel.loadNextTvPage() },
                                                onSeeMore: {
                                                    navigationManager.goToDiscoverCategory(category: .popularSeries)
                                                }
                                            )
                                        case .upcomingMovies:
                                            DiscoverSection(
                                                title: MR.strings().upcoming_movies.localized(),
                                                icon: "calendar",
                                                data: viewModel.upcomingMoviesState,
                                                onItemClick: { item in
                                                    navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                                                },
                                                onItemClickArr: { result in
                                                    handleItemClick(result)
                                                },
                                                onLoadMore: { viewModel.loadNextUpcomingMoviesPage() },
                                                onSeeMore: {
                                                    navigationManager.goToDiscoverCategory(category: .upcomingMovies)
                                                }
                                            )
                                        case .upcomingSeries:
                                            DiscoverSection(
                                                title: MR.strings().upcoming_series.localized(),
                                                icon: "calendar",
                                                data: viewModel.upcomingTvState,
                                                onItemClick: { item in
                                                    navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                                                },
                                                onItemClickArr: { result in
                                                    handleItemClick(result)
                                                },
                                                onLoadMore: { viewModel.loadNextUpcomingTvPage() },
                                                onSeeMore: {
                                                    navigationManager.goToDiscoverCategory(category: .upcomingSeries)
                                                }
                                            )
                                        }
                                    }
                                }
                                .padding(.vertical, 16)
                            }
                            .refreshable {
                                viewModel.refresh()
                            }
                        }
                    } else {
                        DiscoverSearchOverlay(
                            items: viewModel.searchResults,
                            isLoading: viewModel.isSearching,
                            showBanners: viewModel.searchShowBanners,
                            onItemClick: { result in
                                handleItemClick(result)
                            }
                        )
                    }
                }
            }
        }
        .navigationTitle(MR.strings().discover.localized())
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(context == .mainTab)
        .searchable(text: $searchQuery, placement: .navigationBarDrawer(displayMode: .always))
        .onChange(of: searchQuery) { _, newValue in
            viewModel.updateSearchQuery(newValue)
        }
        .toolbar {
            if context == .mainTab {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        navigationManager.showLauncher = true
                    } label: {
                        Image(systemName: "line.3.horizontal")
                    }
                }
            }
            if instancesViewModel.instancesState.selectedInstance != nil {
                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Button {
                            showCustomizationSheet = true
                        } label: {
                            Label(MR.strings().reorganize_hide_sections.localized(), systemImage: "slider.horizontal.3")
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                    }
                }
            }
        }
        .sheet(isPresented: $showCustomizationSheet) {
            DiscoverSectionCustomizationSheet(
                preferences: viewModel.discoverSectionPreferences,
                onUpdatePreferences: { viewModel.updateDiscoverSectionPreferences($0) },
                onResetPreferences: { viewModel.resetDiscoverSectionPreferences() }
            )
        }
    }

    private func handleItemClick(_ result: SearchResult) {
        if let arrResult = result as? SearchResultArrMediaResult {
            navigationManager.goToArrDetailsOrPreview(item: arrResult.media, type: arrResult.instanceType, instanceId: arrResult.instanceId?.int64Value)
        } else if let seerrMedia = result as? SearchResultSeerrMediaResult {
            navigationManager.goToSeerrDetails(tmdbId: seerrMedia.result.id, requestType: seerrMedia.result.mediaType)
        } else if let seerrPerson = result as? SearchResultSeerrPersonResult {
            navigationManager.goToPersonDetails(id: seerrPerson.result.id)
        }
    }
}

struct DiscoverSearchOverlay: View {
    let items: [SearchResult]
    let isLoading: Bool
    let showBanners: Bool
    let onItemClick: (SearchResult) -> Void

    @State private var selectedFilter: InstanceType? = nil

    private var availableFilters: [InstanceType] {
        var seen = Set<InstanceType>()
        return items.compactMap { item in
            if seen.insert(item.instanceType).inserted {
                return item.instanceType
            }
            return nil
        }
    }

    private var itemCounts: [InstanceType: Int] {
        Dictionary(grouping: items, by: { $0.instanceType })
            .mapValues { $0.count }
    }

    private var filteredItems: [SearchResult] {
        if let selectedFilter {
            return items.filter { $0.instanceType == selectedFilter }
        }
        return items
    }

    var body: some View {
        Group {
            if isLoading && items.isEmpty {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if !items.isEmpty {
                VStack(spacing: 0) {
                    MediaInstanceFilterGlassRow(
                        selectedFilter: selectedFilter,
                        availableFilters: availableFilters,
                        itemCounts: itemCounts,
                        totalCount: items.count,
                        onFilterSelected: { filter in
                            selectedFilter = filter
                        }
                    )

                    if filteredItems.isEmpty && selectedFilter != nil {
                        ContentUnavailableView {
                            Label("No \(selectedFilter?.name ?? "") Results", systemImage: "line.3.horizontal.decrease.circle")
                        } description: {
                            Text("No results match the selected media type filter.")
                        } actions: {
                            Button(MR.strings().all.localized()) {
                                selectedFilter = nil
                            }
                            .buttonStyle(.borderedProminent)
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        List {
                            ForEach(filteredItems, id: \.id) { item in
                                DiscoverSearchResultRow(
                                    item: item,
                                    showBanners: showBanners,
                                    onItemClick: onItemClick
                                )
                                .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                                .listRowSeparator(.hidden)
                            }
                        }
                        .listStyle(.plain)
                    }
                }
                .onChange(of: availableFilters) { _, newFilters in
                    if let current = selectedFilter, !newFilters.contains(current) {
                        selectedFilter = nil
                    }
                }
            }
        }
    }
}

struct DiscoverSearchResultRow: View {
    let item: SearchResult
    let showBanners: Bool
    let onItemClick: (SearchResult) -> Void

    private var edgeColor: Color {
        item.instanceType.associatedColor.toSwiftUI()
    }

    var body: some View {
        Group {
            if let arrResult = item as? SearchResultArrMediaResult {
                MediaItemView(
                    item: arrResult.media,
                    aspectRatio: .poster,
                    instanceType: arrResult.instanceType,
                    showBannerBackground: showBanners,
                    includeOverview: true,
                    edgeColor: edgeColor
                )
            } else if let seerrMedia = item as? SearchResultSeerrMediaResult {
                SeerrMediaSearchResultView(
                    result: seerrMedia,
                    showBannerBackground: showBanners,
                    edgeColor: edgeColor
                )
            } else if let seerrPerson = item as? SearchResultSeerrPersonResult {
                SeerrPersonSearchResultView(
                    result: seerrPerson,
                    edgeColor: edgeColor
                )
            }
        }
        .onTapGesture {
            onItemClick(item)
        }
    }
}

struct SeerrMediaSearchResultView: View {
    let result: SearchResultSeerrMediaResult
    let showBannerBackground: Bool
    var edgeColor: Color? = InstanceType.seerr.associatedColor.toSwiftUI()

    var body: some View {
        let item = result.result
        HStack(spacing: 0) {
            if let edgeColor = edgeColor {
                Rectangle()
                    .fill(edgeColor)
                    .frame(width: 6)
            }

            HStack(alignment: .top, spacing: 16) {
                GenericPosterItem(
                    posterUrl: item.fullPosterPath,
                    aspectRatio: .poster,
                    posterHeight: 150
                )
                .frame(width: 100, height: 150)

                VStack(alignment: .leading, spacing: 4) {
                    Text(item.title ?? item.name ?? MR.strings().unknown.localized())
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(showBannerBackground ? .white : .primary)
                        .lineLimit(1)

                    let releaseDate = item.releaseDate ?? item.firstAirDate
                    let year = releaseDate.map { String($0.prefix(4)) }
                    let secondLine = [year, item.mediaType.name].compactMap { $0 }.joined(separator: " • ")
                    if !secondLine.isEmpty {
                        Text(secondLine)
                            .font(.system(size: 14))
                            .foregroundColor(showBannerBackground ? .white.opacity(0.8) : .secondary)
                    }

                    if let overview = item.overview {
                        Text(overview)
                            .font(.system(size: 14))
                            .lineLimit(4)
                            .foregroundColor(showBannerBackground ? .white.opacity(0.7) : .secondary)
                            .padding(.top, 4)
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(12)
        }
        .background {
            ZStack {
                if showBannerBackground {
                    if let backdrop = item.backdropPath {
                        AsyncImage(url: URL(string: "https://image.tmdb.org/t/p/original\(backdrop)")) { image in
                            image.resizable().aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Color.gray
                        }
                        .blur(radius: 10)
                    }
                    Color.black.opacity(0.5)
                } else {
                    Color(.systemBackground)
                }
            }
        }
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 4)
    }
}

struct SeerrPersonSearchResultView: View {
    let result: SearchResultSeerrPersonResult
    var edgeColor: Color? = InstanceType.seerr.associatedColor.toSwiftUI()

    var body: some View {
        let item = result.result
        HStack(spacing: 0) {
            if let edgeColor = edgeColor {
                Rectangle()
                    .fill(edgeColor)
                    .frame(width: 6)
            }

            HStack(alignment: .top, spacing: 16) {
                AsyncImage(url: URL(string: item.fullPosterPath ?? "")) { image in
                    image.resizable().aspectRatio(contentMode: .fill)
                } placeholder: {
                    ZStack {
                        Color(.systemGray4)
                        Image(systemName: "person.fill")
                            .foregroundColor(.gray)
                    }
                }
                .frame(width: 88, height: 88)
                .clipShape(Circle())

                VStack(alignment: .leading, spacing: 4) {
                    Text(item.name ?? MR.strings().unknown.localized())
                        .font(.system(size: 18, weight: .bold))
                        .lineLimit(1)

                    let knownFor = item.knownFor.compactMap { $0.title ?? $0.name }.joined(separator: ", ")
                    if !knownFor.isEmpty {
                        Text("Known for: \(knownFor)")
                            .font(.system(size: 14))
                            .foregroundColor(.secondary)
                            .lineLimit(2)
                    }

                    if let overview = item.overview {
                        Text(overview)
                            .font(.system(size: 14))
                            .lineLimit(3)
                            .foregroundColor(.secondary)
                            .padding(.top, 4)
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(12)
        }
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.1), radius: 4)
    }
}
