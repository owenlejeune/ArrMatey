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
    @ObservedObject var viewModel: ArrMediaViewModelS
    let state: ArrLibrarySuccess
    @Binding var searchQuery: String
    @Binding var searchPresented: Bool
    
    @EnvironmentObject private var navigation: NavigationManager
    
    @ObservedObject private var activityQueueViewModel = ActivityQueueViewModelS()
    
    @State private var confirmDelete: Bool = false
    @State private var showMonitorOptions: Bool = false

    private var queueItems: [QueueItem] {
        activityQueueViewModel.queueItems
    }
    
    var body: some View {
        ZStack(alignment: .bottom) {
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
            
            if viewModel.isInSelectionMode {
                selectionBottomBar
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .zIndex(1)
            }
        }
        .searchable(
            text: $searchQuery,
            isPresented: $searchPresented,
            placement: .navigationBarDrawer(displayMode: .automatic)
        )
        .toolbar {
            toolbarContent
        }
        .sheet(isPresented: $confirmDelete) {
            DeleteMediaSheet(isLoading: false) { addExclusion, deleteFiles in
                if viewModel.isInSelectionMode {
                    viewModel.deleteSelected(deleteFiles: deleteFiles, addExclusion: addExclusion)
                } else if let item = selectedItemForAction {
                    viewModel.deleteMedia(item, deleteFiles: deleteFiles, addImportExclusion: addExclusion)
                }
                confirmDelete = false
            }
            .presentationDetents([.medium])
        }
        .sheet(isPresented: $showMonitorOptions) {
            MonitorOptionsSheet(type: type) { option in
                viewModel.updateMonitoringSelected(option)
            }
            .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showEditSheet) {
            if let item = selectedItemForEdit {
                editSheet(for: item)
            }
        }
    }
    
    @State private var showEditSheet: Bool = false
    @State private var selectedItemForEdit: ArrMedia? = nil
    
    @ViewBuilder
    private func editSheet(for item: ArrMedia) -> some View {
        let profiles = viewModel.instanceData?.qualityProfiles ?? []
        let folders = viewModel.instanceData?.rootFolders ?? []
        let tags = viewModel.instanceData?.tags ?? []
        let isInProgress = viewModel.editItemStatus is OperationStatusInProgress

        if let series = item as? ArrSeries {
            EditSeriesSheet(item: series, qualityProfiles: profiles, rootFolders: folders, tags: tags, editInProgress: isInProgress) { newItem, moveFiles in
                viewModel.editItem(newItem, moveFiles: moveFiles)
            }
        } else if let movie = item as? ArrMovie {
            EditMovieSheet(item: movie, qualityProfiles: profiles, rootFolders: folders, tags: tags, editInProgress: isInProgress) { newItem, moveFiles in
                viewModel.editItem(newItem, moveFiles: moveFiles)
            }
        } else if let artist = item as? Arrtist {
            EditArtistSheet(item: artist, qualityProfiles: profiles, rootFolders: folders, tags: tags, editInProgress: isInProgress) { newItem, moveFiles in
                viewModel.editItem(newItem, moveFiles: moveFiles)
            }
        } else if let author = item as? Author {
            EditAuthorSheet(item: author, qualityProfiles: profiles, rootFolders: folders, tags: tags, editInProgress: isInProgress) { newItem, moveFiles in
                viewModel.editItem(newItem, moveFiles: moveFiles)
            }
        } else if let audiobook = item as? Audiobook {
            EditAudiobookSheet(item: audiobook, qualityProfiles: profiles, rootFolders: folders, editInProgress: isInProgress) { newItem in
                viewModel.editItem(newItem)
            }
        }
    }
    
    @State private var selectedItemForAction: ArrMedia? = nil
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if viewModel.isInSelectionMode {
            ToolbarItem(placement: .topBarLeading) {
                Button(MR.strings().close.localized()) {
                    viewModel.exitSelectionMode()
                }
            }
            
            ToolbarItem(placement: .principal) {
                Text(MR.strings().selected_count.format(args: [viewModel.selectionCount]).localized())
                    .font(.headline)
            }
            
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: {
                    if viewModel.areAllItemsSelected() {
                        viewModel.clearSelection()
                    } else {
                        viewModel.selectAllItems()
                    }
                }) {
                    Image(systemName: viewModel.areAllItemsSelected() ? "checkmark.circle.fill" : "circle")
                }
            }
        }
    }
    
    private var selectionBottomBar: some View {
        HStack {
            if viewModel.selectionCount == 1 {
                Button(action: {
                    selectedItemForEdit = viewModel.selectedItem
                    showEditSheet = true
                }) {
                    Label(MR.strings().edit.localized(), systemImage: "pencil")
                }
                
                Spacer()
                
                Button(action: {
                    viewModel.toggleMonitoringForSelected()
                }) {
                    let isMonitored = viewModel.selectedItem?.monitored == true
                    Label(isMonitored ? MR.strings().monitored.localized() : MR.strings().unmonitored.localized(),
                          systemImage: isMonitored ? "bookmark.fill" : "bookmark")
                }
                
                Spacer()
            }
            
            Menu {
                Button(action: { viewModel.refreshSelected() }) {
                    Label(MR.strings().refresh.localized(), systemImage: "arrow.clockwise")
                }
                
                Button(action: { viewModel.performAutomaticLookupSelected() }) {
                    Label(MR.strings().search_monitored.localized(), systemImage: "magnifyingglass")
                }
                
                if viewModel.hasBazarr && (type == .sonarr || type == .radarr) {
                    Button(action: { viewModel.performSubtitleSearchSelected() }) {
                        Label(MR.strings().bazarr_search_subtitles.localized(), systemImage: "captions.bubble")
                    }
                }
                
                if type != .radarr {
                    Button(action: { showMonitorOptions = true }) {
                        Label(MR.strings().update_monitoring.localized(), systemImage: "bookmark.circle")
                    }
                }
            } label: {
                Image(systemName: "ellipsis.circle")
            }
            
            Spacer()
            
            Button(role: .destructive, action: {
                confirmDelete = true
            }) {
                Label(MR.strings().delete.localized(), systemImage: "trash")
            }
            .foregroundColor(.red)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .background(
            RoundedRectangle(cornerRadius: 15)
                .fill(Color(uiColor: .systemBackground))
                .shadow(radius: 10)
        )
        .padding(.horizontal, 16)
        .padding(.bottom, 20)
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
                        if viewModel.isInSelectionMode {
                            if let id = media.id?.int64Value {
                                viewModel.toggleItemSelection(id)
                            }
                        } else {
                            if let id = media.id?.int64Value {
                                navigation.go(to: .details(id: id, type: type), of: type)
                            }
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
                        let isSelected = viewModel.selectedItems.contains(item.id?.int64Value ?? -1)
                        
                        ZStack(alignment: .topTrailing) {
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
                            .contextMenu {
                                itemContextMenu(item)
                            }
                            
                            if viewModel.isInSelectionMode {
                                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                                    .foregroundColor(isSelected ? .blue : .white)
                                    .background(Circle().fill(isSelected ? .white : .black.opacity(0.3)))
                                    .padding(8)
                            }
                        }
                    }
                }
                .padding(16)
                .padding(.bottom, viewModel.isInSelectionMode ? 100 : 0)
            } else {
                LazyVStack(spacing: 12) {
                    ForEach(items, id: \.id) { item in
                        let isSelected = viewModel.selectedItems.contains(item.id?.int64Value ?? -1)

                        HStack {
                            if viewModel.isInSelectionMode {
                                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                                    .foregroundColor(isSelected ? .blue : .secondary)
                                    .onTapGesture {
                                        if let id = item.id?.int64Value {
                                            viewModel.toggleItemSelection(id)
                                        }
                                    }
                            }
                            
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
                            .contextMenu {
                                itemContextMenu(item)
                            }
                        }
                    }
                }
                .padding(16)
                .padding(.bottom, viewModel.isInSelectionMode ? 100 : 0)
            }
        }
    }
    
    @ViewBuilder
    private func itemContextMenu(_ item: ArrMedia) -> some View {
        Button(action: {
            if let id = item.id?.int64Value {
                viewModel.toggleItemSelection(id)
                viewModel.enterSelectionMode()
            }
        }) {
            Label("Select", systemImage: "checkmark.circle")
        }
        
        Divider()
        
        Button(action: {
            selectedItemForEdit = item
            showEditSheet = true
        }) {
            Label(MR.strings().edit.localized(), systemImage: "pencil")
        }
        
        Button(action: {
            viewModel.toggleMonitored(item)
        }) {
            Label(item.monitored ? MR.strings().unmonitored.localized() : MR.strings().monitored.localized(),
                  systemImage: item.monitored ? "bookmark" : "bookmark.fill")
        }
        
        Button(action: {
            viewModel.performRefresh(item)
        }) {
            Label(MR.strings().refresh.localized(), systemImage: "arrow.clockwise")
        }
        
        Button(action: {
            viewModel.performAutomaticLookup(item)
        }) {
            Label(MR.strings().search.localized(), systemImage: "magnifyingglass")
        }
        
        if viewModel.hasBazarr && (type == .sonarr || type == .radarr) {
            Button(action: {
                viewModel.performSubtitleSearch(item: item)
            }) {
                Label(MR.strings().bazarr_search_subtitles.localized(), systemImage: "captions.bubble")
            }
        }
        
        if type != .radarr {
            Button(action: {
                if let id = item.id?.int64Value {
                    viewModel.toggleItemSelection(id)
                    showMonitorOptions = true
                }
            }) {
                Label(MR.strings().update_monitoring.localized(), systemImage: "bookmark.circle")
            }
        }
        
        Divider()
        
        Button(role: .destructive, action: {
            selectedItemForAction = item
            confirmDelete = true
        }) {
            Label(MR.strings().delete.localized(), systemImage: "trash")
        }
    }
}
