//
//  ArrLibraryView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-20.
//

import SwiftUI
import Shared

struct ArrLibraryView: View {
    let type: InstanceType
    let state: ArrLibrarySuccess
    @Binding var searchQuery: String
    @Binding var searchPresented: Bool
    
    @EnvironmentObject private var navigation: NavigationManager
    
    @ObservedObject private var activityQueueViewModel = ActivityQueueViewModelS()
    
    private var queueItems: [QueueItem] {
        activityQueueViewModel.queueItems
    }
    
    var body: some View {
        Group {
            if state.items.isEmpty && searchQuery.isEmpty {
                VStack {
                    EmptyLibraryView()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                contentView(items: state.items, prefs: state.preferences)
            }
        }
        .searchable(
            text: $searchQuery,
            isPresented: $searchPresented,
            placement: .navigationBarDrawer(displayMode: .automatic)
        )
    }
    
    private func contentView(
        items: [ArrMedia],
        prefs: InstancePreferences
    ) -> some View {
        VStack(spacing: 0) {
            if items.isEmpty {
                EmptySearchResultsView(type: type, query: searchQuery, onShouldSearch: {
                    navigation.go(to: .search(query: searchQuery, type: type), of: type)
                })
            } else {
                mediaView(
                    viewType: prefs.viewType,
                    aspectRatio: type.aspectRatio,
                    items: items,
                    prefs: prefs,
                    onItemClicked: { media in
                        if let id = media.id?.int64Value {
                            navigation.go(to: .details(id: id, type: type), of: type)
                        }
                    },
                    itemIsActive: { item in
                        queueItems.contains(where: { $0.mediaId == item.id })
                    }
                )
            }
        }
        .id(items.count)
    }
    
    @ViewBuilder
    private func mediaView(
        viewType: ViewType,
        aspectRatio: AspectRatio,
        items: [ArrMedia],
        prefs: InstancePreferences,
        onItemClicked: @escaping (ArrMedia) -> Void,
        itemIsActive: @escaping (ArrMedia) -> Bool
    ) -> some View {
        ScrollView {
            if viewType == .grid {
                let columns = [GridItem(.adaptive(minimum: prefs.gridDensity.iosSize), spacing: prefs.gridSpacing.iosSpacing)]

                
                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(items, id: \.id) { item in
                        PosterItem(
                            item: item,
                            instanceType: type,
                            aspectRatio: aspectRatio,
                            elevation: prefs.posterElevation,
                            radius: prefs.posterRadius,
                            showFooter: prefs.showFullDetails,
                            onItemClick: { item in onItemClicked(item) }
                        ) {
                            if prefs.showOverlay {
                                VStack {
                                    HStack {
                                        if item.id != nil {
                                            Image(systemName: item.monitored ? "bookmark.fill" : "bookmark")
                                                .foregroundColor(.white)
                                                .padding(8)
                                        }
                                        Spacer()
                                    }
                                    Spacer()
                                    if item.id != nil {
                                        ProgressView(value: Double(item.statusProgress))
                                            .tint(itemIsActive(item) ? Color.blue : Color(argb: item.statusColor))
                                            .padding(8)
                                    }
                                }
                            }
                        }
                    }
                }
                .padding(16)
            } else {
                LazyVStack(spacing: 12) {
                    ForEach(items, id: \.id) { item in
                        MediaItemView(
                            item: item,
                            aspectRatio: aspectRatio,
                            instanceType: type,
                            isActive: itemIsActive(item),
                            showBannerBackground: prefs.showBannerBackground,
                            includeOverview: prefs.includeOverview,
                            bannerBlur: prefs.bannerBlur,
                            posterElevation: prefs.posterElevation,
                            posterRadius: prefs.posterRadius
                        )
                        .onTapGesture {
                            onItemClicked(item)
                        }
                    }
                }
                .padding(16)
            }
        }
    }
}
