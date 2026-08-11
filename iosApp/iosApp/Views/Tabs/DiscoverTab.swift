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
                        seerrDestination(for: route)
                    }
            }
        case .launcher:
            DiscoverTabContent()
                .navigationDestination(for: SeerrRoute.self) { route in
                    seerrDestination(for: route)
                }
        }
    }
    
    @ViewBuilder
    private func seerrDestination(for route: SeerrRoute) -> some View {
        switch route {
        case .details(let tmdbId, let requestType):
            SeerrDetailsScreen(tmdbId: tmdbId, requestType: requestType)
        }
    }
}

private struct DiscoverTabContent: View {
    @StateObject private var viewModel = TrendingViewModelS()
    @StateObject private var instancesViewModel = InstancesViewModelS(type: .seerr)
    @EnvironmentObject private var navigationManager: NavigationManager
    @Environment(\.navigationContext) private var context

    var body: some View {
        Group {
            if instancesViewModel.instancesState.selectedInstance == nil {
                NoInstanceView(type: .seerr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 24) {
                        DiscoverSection(
                            title: MR.strings().trending.localized(),
                            icon: "chart.line.uptrend.xyaxis",
                            data: viewModel.trendingState,
                            onItemClick: { item in
                                navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
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
                            onLoadMore: { viewModel.loadNextMoviesPage() }
                        )

                        DiscoverSection(
                            title: MR.strings().popular_series.localized(),
                            icon: "tv",
                            data: viewModel.tvState,
                            onItemClick: { item in
                                navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
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
                            onLoadMore: { viewModel.loadNextUpcomingMoviesPage() }
                        )

                        DiscoverSection(
                            title: MR.strings().upcoming_series.localized(),
                            icon: "calendar",
                            data: viewModel.upcomingTvState,
                            onItemClick: { item in
                                navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                            },
                            onLoadMore: { viewModel.loadNextUpcomingTvPage() }
                        )
                    }
                    .padding(.vertical, 16)
                }
                .refreshable {
                    viewModel.refresh()
                }
            }
        }
        .navigationTitle(MR.strings().discover.localized())
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(context == .mainTab)
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
}

private struct DiscoverSection: View {
    let title: String
    let icon: String
    let data: PagedData<DiscoverResult>
    let onItemClick: (DiscoverResult) -> Void
    let onLoadMore: () -> Void
    
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
                                onItemClick: onItemClick
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
