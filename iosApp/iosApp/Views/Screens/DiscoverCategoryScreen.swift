//
//  DiscoverCategoryScreen.swift
//  iosApp
//

import SwiftUI
import Shared

struct DiscoverCategoryScreen: View {
    let category: DiscoverCategory
    @StateObject private var viewModel = DiscoverViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager
    @State private var viewType: ViewType = .grid

    private var gridColumns: [GridItem] {
        [GridItem(.adaptive(minimum: 120, maximum: 160), spacing: 12)]
    }

    var body: some View {
        let data = viewModel.getStateForCategory(category)
        Group {
            if data.isLoading && data.items.isEmpty {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if !data.items.isEmpty {
                ScrollView {
                    if viewType == .grid {
                        LazyVGrid(columns: gridColumns, spacing: 12) {
                            ForEach(data.items as! [DiscoverResult], id: \.id) { item in
                                DiscoverPosterItem(
                                    item: item,
                                    elevation: .none,
                                    posterHeight: 180,
                                    onItemClick: { result in
                                        navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                                    },
                                    showOverlays: true
                                )
                                .onAppear {
                                    if item.id == (data.items.last as? DiscoverResult)?.id {
                                        viewModel.loadNextPageForCategory(category)
                                    }
                                }
                            }

                            if data.isLoadingMore {
                                ProgressView()
                                    .padding(.vertical, 16)
                            } else if data.loadMoreFailed {
                                Button(action: { viewModel.loadNextPageForCategory(category) }) {
                                    Image(systemName: "arrow.clockwise")
                                        .font(.system(size: 16, weight: .medium))
                                        .foregroundColor(.accentColor)
                                        .padding(8)
                                }
                                .buttonStyle(.plain)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                            }
                        }
                        .padding(16)
                    } else {
                        LazyVStack(spacing: 12) {
                            ForEach(data.items as! [DiscoverResult], id: \.id) { item in
                                SeerrMediaSearchResultView(
                                    result: SearchResultSeerrMediaResult(result: item, originalRank: 0),
                                    showBannerBackground: true
                                )
                                .contentShape(Rectangle())
                                .onTapGesture {
                                    navigationManager.goToSeerrDetails(tmdbId: item.id, requestType: item.mediaType)
                                }
                                .onAppear {
                                    if item.id == (data.items.last as? DiscoverResult)?.id {
                                        viewModel.loadNextPageForCategory(category)
                                    }
                                }
                            }

                            if data.isLoadingMore {
                                ProgressView()
                                    .padding(.vertical, 16)
                            } else if data.loadMoreFailed {
                                Button(action: { viewModel.loadNextPageForCategory(category) }) {
                                    Image(systemName: "arrow.clockwise")
                                        .font(.system(size: 16, weight: .medium))
                                        .foregroundColor(.accentColor)
                                        .padding(8)
                                }
                                .buttonStyle(.plain)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 16)
                            }
                        }
                        .padding(16)
                    }
                }
            }
        }
        .navigationTitle(category.title.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    viewType = viewType == .grid ? .list : .grid
                }) {
                    Image(systemName: viewType == .grid ? "rectangle.grid.1x2" : "rectangle.grid.2x2")
                }
            }
        }
    }
}
