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

    var body: some View {
        Group {
            if instancesViewModel.instancesState.selectedInstance == nil {
                NoInstanceView(type: .seerr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                Group {
                    if searchQuery.isEmpty {
                        ScrollView {
                            VStack(alignment: .leading, spacing: 24) {
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
                                    onLoadMore: { viewModel.loadNextTrendingPage() }
                                )

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
                                    onLoadMore: { viewModel.loadNextMoviesPage() }
                                )

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
                                    onLoadMore: { viewModel.loadNextTvPage() }
                                )

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
                                    onLoadMore: { viewModel.loadNextUpcomingMoviesPage() }
                                )

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
                                    onLoadMore: { viewModel.loadNextUpcomingTvPage() }
                                )
                            }
                            .padding(.vertical, 16)
                        }
                        .refreshable {
                            viewModel.refresh()
                        }
                    } else {
                        DiscoverSearchOverlay(
                            items: viewModel.searchResults,
                            isLoading: viewModel.isSearching,
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
        }
    }
    
    private func handleItemClick(_ result: SearchResult) {
        if let arrResult = result as? SearchResultArrMediaResult {
            let item = arrResult.media
            if let movie = item as? ArrMovie {
                navigationManager.goToDetails(id: movie.id, tmdbId: movie.tmdbId, type: .radarr)
            } else if let series = item as? ArrSeries {
                navigationManager.goToDetails(id: series.id, tmdbId: series.tmdbId, tvdbId: series.tvdbId, type: .sonarr)
            }
        } else if let seerrMedia = result as? SearchResultSeerrMediaResult {
            navigationManager.goToSeerrDetails(tmdbId: seerrMedia.result.id, requestType: seerrMedia.result.mediaType)
        } else if let seerrPerson = result as? SearchResultSeerrPersonResult {
            navigationManager.goToSeerrDetails(tmdbId: seerrPerson.result.id, requestType: .person)
        }
    }
}

private struct DiscoverSection: View {
    let title: String
    let icon: String
    let data: PagedData<DiscoverResult>
    let onItemClick: (DiscoverResult) -> Void
    let onItemClickArr: ((SearchResult) -> Void)?
    let onLoadMore: () -> Void
    var showOverlays: Bool = true
    
    init(
        title: String,
        icon: String,
        data: PagedData<DiscoverResult>,
        onItemClick: @escaping (DiscoverResult) -> Void,
        onItemClickArr: ((SearchResult) -> Void)? = nil,
        onLoadMore: @escaping () -> Void,
        showOverlays: Bool = true
    ) {
        self.title = title
        self.icon = icon
        self.data = data
        self.onItemClick = onItemClick
        self.onItemClickArr = onItemClickArr
        self.onLoadMore = onLoadMore
        self.showOverlays = showOverlays
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 20))
                Text(title)
                    .font(.headline)
            }
            .padding(.horizontal, 16)
            
            if data.isLoading && data.items.isEmpty {
                HStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
                .padding(.vertical, 24)
            } else if !data.items.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(alignment: .top, spacing: 12) {
                        ForEach(data.items as! [DiscoverResult], id: \.id) { item in
                            DiscoverPosterItem(
                                item: item,
                                elevation: .none,
                                posterHeight: 180,
                                onItemClick: { result in
                                    if let onItemClickArr = onItemClickArr {
                                        onItemClickArr(SearchResultSeerrMediaResult(result: result))
                                    } else {
                                        onItemClick(result)
                                    }
                                },
                                showOverlays: showOverlays
                            )
                            .onAppear {
                                if item.id == (data.items.last as? DiscoverResult)?.id {
                                    onLoadMore()
                                }
                            }
                        }
                        
                        if data.isLoadingMore {
                            ProgressView()
                                .padding(.horizontal, 16)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12) // Ensure shadows aren't cut off
                }
            } else if let error = data.error {
                Text(error)
                    .foregroundColor(.red)
                    .padding(.horizontal, 16)
            }
        }
    }
}

private struct DiscoverSearchOverlay: View {
    let items: [SearchResult]
    let isLoading: Bool
    let onItemClick: (SearchResult) -> Void
    
    var body: some View {
        Group {
            if isLoading && items.isEmpty {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if !items.isEmpty {
                List {
                    ForEach(items, id: \.id) { item in
                        SearchResultRow(
                            item: item,
                            onItemClick: onItemClick
                        )
                        .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                        .listRowSeparator(.hidden)
                    }
                }
                .listStyle(.plain)
            }
        }
    }
}

private struct SearchResultRow: View {
    let item: SearchResult
    let onItemClick: (SearchResult) -> Void
    
    var body: some View {
        Group {
            if let arrResult = item as? SearchResultArrMediaResult {
                MediaItemView(
                    item: arrResult.media,
                    aspectRatio: .poster,
                    showBannerBackground: true,
                    includeOverview: true
                )
            } else if let seerrMedia = item as? SearchResultSeerrMediaResult {
                SeerrMediaSearchResultView(result: seerrMedia)
            } else if let seerrPerson = item as? SearchResultSeerrPersonResult {
                SeerrPersonSearchResultView(result: seerrPerson)
            }
        }
        .onTapGesture {
            onItemClick(item)
        }
    }
}

struct SeerrMediaSearchResultView: View {
    let result: SearchResultSeerrMediaResult
    
    var body: some View {
        let item = result.result
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top, spacing: 18) {
                GenericPosterItem(
                    posterUrl: item.fullPosterPath,
                    aspectRatio: .poster
                )
                .frame(height: 75)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(item.title ?? item.name ?? MR.strings().unknown.localized())
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                    
                    if let date = item.releaseDate ?? item.firstAirDate {
                        Text(String(date.prefix(4)))
                            .font(.system(size: 14))
                            .foregroundColor(.white.opacity(0.8))
                    }
                    
                    if let overview = item.overview {
                        Text(overview)
                            .font(.system(size: 14))
                            .lineLimit(3)
                            .foregroundColor(.white.opacity(0.7))
                    }
                }
            }
            .padding(12)
        }
        .background {
            ZStack {
                if let backdrop = item.backdropPath {
                    AsyncImage(url: URL(string: "https://image.tmdb.org/t/p/original\(backdrop)")) { image in
                        image.resizable().aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Color.gray
                    }
                    .blur(radius: 10)
                }
                Color.black.opacity(0.5)
            }
        }
        .cornerRadius(12)
    }
}

struct SeerrPersonSearchResultView: View {
    let result: SearchResultSeerrPersonResult
    
    var body: some View {
        let item = result.result
        HStack(alignment: .top, spacing: 18) {
            GenericPosterItem(
                posterUrl: item.fullPosterPath,
                aspectRatio: .poster
            )
            .frame(height: 75)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(item.name ?? MR.strings().unknown.localized())
                    .font(.system(size: 18, weight: .bold))
                
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
                        .lineLimit(2)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding(12)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.1), radius: 4)
    }
}
