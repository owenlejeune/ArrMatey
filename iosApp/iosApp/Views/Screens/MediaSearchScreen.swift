//
//  MediaSearchScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-27.
//

import SwiftUI
import Shared

struct MediaSearchScreen: View {
    private let type: InstanceType
    
    @ObservedObject private var viewModel: ArrSearchViewModelS
    
    @Environment(\.dismiss) var dismiss
    @EnvironmentObject private var navigation: NavigationManager
    
    @State private var searchQuery: String
    @State private var searchPresented: Bool = false
    
    init(query: String, type: InstanceType, instanceId: Int64? = nil) {
        self.searchQuery = query
        self.type = type
        self.viewModel = ArrSearchViewModelS(type: type, instanceId: instanceId)
    }
    
    private var uiState: ArrLibrary {
        viewModel.uiState
    }
    
    var body: some View {
        contentForState()
            .task {
                try? await Task.sleep(nanoseconds: 500_000_000)
                searchPresented = true
            }
            .onDebounceSearch(searchQuery, initial: true) { query in
                guard !query.isEmpty else { return }
                viewModel.performLookup(query)
            }
            .toolbar {
                toolbarContent
            }
            .searchable(text: $searchQuery, isPresented: $searchPresented, placement: .navigationBarDrawer)
    }
    
    @ViewBuilder
    private func contentForState() -> some View {
        if uiState is ArrLibraryInitial {
            Color.clear
        } else if uiState is ArrLibraryLoading {
            ZStack {
                ProgressView()
                    .progressViewStyle(.circular)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if let state = uiState as? ArrLibrarySuccess {
            resultsArea(state)
        } else if uiState is ArrLibraryError {
            Text("error state")
        } else {
            EmptyView()
        }
    }
    
    @ViewBuilder
    private func resultsArea(_ state: ArrLibrarySuccess) -> some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(state.items, id: \.guid) { item in
                    MediaItemView(
                        item: item,
                        aspectRatio: type.aspectRatio,
                        isActive: viewModel.activeMediaIds.contains(item.id?.int64Value ?? 0),
                        showBannerBackground: viewModel.searchShowBanners,
                        includeOverview: true
                    )
                        .id(item.guid)
                        .onTapGesture {
                            navigation.goToArrDetailsOrPreview(item: item, type: type)
                        }
                }
            }
            .padding(16)
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .primaryAction) {
            SortByPickerMenu(type: type, sortBy: viewModel.sortBy, sortOrder: viewModel.sortOrder, changeSortBy: { viewModel.setSortBy($0) }, changeSortOrder: { viewModel.setSortOrder($0) }, limitToLookup: true)
        }
    }
}
