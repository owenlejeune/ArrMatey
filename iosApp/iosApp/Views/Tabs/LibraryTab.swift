//
//  LibraryTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-08-21.
//

import Foundation
import SwiftUI
import Shared

struct LibraryTab: View {
    @StateObject private var libraryViewModel = UnifiedLibraryViewModelS()
    
    var body: some View {
        LibraryTabContent(libraryViewModel: libraryViewModel)
    }
}

struct LibraryTabContent: View {
    @ObservedObject var libraryViewModel: UnifiedLibraryViewModelS
    @EnvironmentObject private var navigationManager: NavigationManager
    
    @State private var searchPresented: Bool = false
    @State private var customizationSheetPresented: Bool = false
    @State private var confirmDelete: Bool = false
    @State private var showEditSheet: Bool = false
    @State private var selectedItemForEdit: ArrMedia? = nil
    @State private var selectedItemForAction: ArrMedia? = nil
    @State private var showMonitorOptions: Bool = false
    
    private var selectedInstance: Instance? {
        libraryViewModel.selectedInstance
    }
    
    private var currentType: InstanceType {
        selectedInstance?.type ?? .sonarr
    }
    
    private var uiState: ArrLibrary {
        libraryViewModel.currentLibraryState
    }
    
    private var preferences: InstancePreferences {
        libraryViewModel.preferences
    }

    var body: some View {
        VStack(spacing: 0) {
            if libraryViewModel.arrInstances.isEmpty || selectedInstance == nil {
                NoInstanceView(type: .sonarr)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let instance = selectedInstance {
                if libraryViewModel.arrInstances.count > 1 && !libraryViewModel.isInSelectionMode {
                    topTabsRow
                }
                
                contentForState(instance: instance)
            }
        }
        .searchable(
            text: $libraryViewModel.searchQuery,
            isPresented: $searchPresented,
            placement: .navigationBarDrawer(displayMode: .automatic)
        )
        .navigationTitle(selectedInstance?.label ?? MR.strings().library.localized())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            toolbarContent
        }
        .refreshable {
            libraryViewModel.refreshSelected()
        }
        .sheet(isPresented: $customizationSheetPresented) {
            ArrViewCustomizationSheet(
                type: currentType,
                viewModel: libraryViewModel
            )
        }
        .sheet(isPresented: $confirmDelete) {
            DeleteMediaSheet(
                isLoading: false,
                initialAddExclusion: preferences.deleteAddExclusion,
                initialDeleteFiles: preferences.deleteDeleteFiles
            ) { addExclusion, deleteFiles in
                if libraryViewModel.isInSelectionMode {
                    libraryViewModel.deleteSelected(deleteFiles: deleteFiles, addExclusion: addExclusion)
                } else if let item = selectedItemForAction {
                    libraryViewModel.deleteMedia(item, deleteFiles: deleteFiles, addExclusion: addExclusion)
                }
                confirmDelete = false
            }
            .presentationDetents([.medium])
        }
        .sheet(isPresented: $showMonitorOptions) {
            MonitorOptionsSheet(type: currentType) { option in
                // Handled via monitor options
            }
            .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showEditSheet) {
            if let item = selectedItemForEdit {
                editSheet(for: item)
            }
        }
        .navigationDestination(for: MediaRoute.self) { value in
            MediaRouteDestination(route: value)
        }
    }
    
    private var topTabsRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(libraryViewModel.arrInstances, id: \.id) { tabInstance in
                    let isSelected = tabInstance.id == selectedInstance?.id
                    let isOffline = libraryViewModel.isInstanceOffline(tabInstance.id)
                    if isSelected {
                        InstanceOptionsMenu(
                            instanceUrl: tabInstance.url,
                            onRunRssSync: { libraryViewModel.runRssSync() },
                            onSearchAllMissing: { libraryViewModel.searchAllMissing() },
                            onUpdateLibrary: { libraryViewModel.updateLibrary() },
                            onBackupDatabase: { libraryViewModel.backupDatabase() }
                        ) {
                            tabPill(tabInstance: tabInstance, isSelected: isSelected, isOffline: isOffline)
                        }
                    } else {
                        tabPill(tabInstance: tabInstance, isSelected: isSelected, isOffline: isOffline)
                            .onTapGesture {
                                libraryViewModel.selectInstance(tabInstance)
                            }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 6)
        }
        .frame(height: 48)
        .background(Color(UIColor.systemBackground))
    }
    
    @ViewBuilder
    private func tabPill(tabInstance: Instance, isSelected: Bool, isOffline: Bool) -> some View {
        HStack(spacing: 6) {
            if let logo = tabInstance.type.tabIcon {
                logo.toImage(renderingMode: .template)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 16, height: 16)
            }
            Text(tabInstance.label)
                .font(.subheadline.weight(isSelected ? .semibold : .regular))
            
            if isOffline {
                Image(systemName: "wifi.slash")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(isSelected ? .white : .red)
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 8)
        .background(isSelected ? Color.themePrimary : Color(UIColor.secondarySystemFill))
        .foregroundColor(isSelected ? Color.white : Color.primary)
        .clipShape(Capsule())
        .contentShape(Capsule())
    }
    
    @ViewBuilder
    private func contentForState(instance: Instance) -> some View {
        if uiState is ArrLibraryInitial || uiState is ArrLibraryLoading {
            ZStack {
                ProgressView()
                    .progressViewStyle(.circular)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if let success = uiState as? ArrLibrarySuccess {
            ZStack(alignment: .bottom) {
                Group {
                    if success.items.isEmpty && libraryViewModel.searchQuery.isEmpty {
                        VStack {
                            EmptyLibraryView()
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        contentView(items: success.items, prefs: preferences)
                    }
                }
                
                if libraryViewModel.isInSelectionMode {
                    selectionBottomBar
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                        .zIndex(1)
                }
            }
        } else if let error = uiState as? ArrLibraryError {
            ZStack {
                ErrorView(
                    errorType: error.type,
                    message: error.message,
                    onOpenSettings: {
                        navigationManager.goToEditInstance(of: currentType, instance.id)
                    },
                    onRetry: { libraryViewModel.refreshSelected() }
                )
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            VStack {
                NoInstanceView(type: currentType)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
    
    private func contentView(
        items: [ArrMedia],
        prefs: InstancePreferences
    ) -> some View {
        VStack(spacing: 0) {
            if items.isEmpty {
                EmptySearchResultsView(type: currentType, query: libraryViewModel.searchQuery, onShouldSearch: {
                    navigationManager.go(to: .search(query: libraryViewModel.searchQuery, type: currentType, instanceId: selectedInstance?.id), of: currentType)
                })
            } else {
                mediaView(
                    viewType: prefs.viewType,
                    aspectRatio: currentType.aspectRatio,
                    items: items,
                    prefs: prefs,
                    onItemClicked: { media in
                        if libraryViewModel.isInSelectionMode {
                            if let id = media.id?.int64Value {
                                libraryViewModel.toggleItemSelection(id)
                            }
                        } else {
                            if let id = media.id?.int64Value {
                                navigationManager.go(to: .details(id: id, type: currentType), of: currentType)
                            }
                        }
                    },
                    itemIsActive: { item in
                        guard let instanceId = selectedInstance?.id, let itemId = item.id?.int64Value else { return false }
                        return libraryViewModel.isItemActive(instanceId: instanceId, mediaId: itemId)
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
                        let isSelected = libraryViewModel.selectedItems.contains(item.id?.int64Value ?? -1)
                        
                        ZStack(alignment: .topTrailing) {
                            PosterItem(
                                item: item,
                                instanceType: currentType,
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
                            
                            if libraryViewModel.isInSelectionMode {
                                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                                    .foregroundColor(isSelected ? .blue : .white)
                                    .background(Circle().fill(isSelected ? .white : .black.opacity(0.3)))
                                    .padding(8)
                            }
                        }
                    }
                }
                .padding(16)
                .padding(.bottom, libraryViewModel.isInSelectionMode ? 100 : 0)
            } else {
                LazyVStack(spacing: 12) {
                    ForEach(items, id: \.id) { item in
                        let isSelected = libraryViewModel.selectedItems.contains(item.id?.int64Value ?? -1)

                        HStack {
                            if libraryViewModel.isInSelectionMode {
                                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                                    .foregroundColor(isSelected ? .blue : .secondary)
                                    .onTapGesture {
                                        if let id = item.id?.int64Value {
                                            libraryViewModel.toggleItemSelection(id)
                                        }
                                    }
                            }
                            
                            MediaItemView(
                                item: item,
                                aspectRatio: aspectRatio,
                                instanceType: currentType,
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
                .padding(.bottom, libraryViewModel.isInSelectionMode ? 100 : 0)
            }
        }
    }
    
    @ViewBuilder
    private func itemContextMenu(_ item: ArrMedia) -> some View {
        Button(action: {
            if let id = item.id?.int64Value {
                libraryViewModel.toggleItemSelection(id)
                libraryViewModel.enterSelectionMode()
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
            libraryViewModel.toggleMonitored(item)
        }) {
            Label(item.monitored ? MR.strings().unmonitored.localized() : MR.strings().monitored.localized(),
                  systemImage: item.monitored ? "bookmark" : "bookmark.fill")
        }
        
        Button(action: {
            libraryViewModel.performRefresh(item)
        }) {
            Label(MR.strings().refresh.localized(), systemImage: "arrow.clockwise")
        }
        
        Button(action: {
            libraryViewModel.performAutomaticLookup(item)
        }) {
            Label(MR.strings().search.localized(), systemImage: "magnifyingglass")
        }
        
        if libraryViewModel.hasBazarr && (currentType == .sonarr || currentType == .radarr) {
            Button(action: {
                libraryViewModel.performSubtitleSearch(item)
            }) {
                Label(MR.strings().bazarr_search_subtitles.localized(), systemImage: "captions.bubble")
            }
        }
        
        if currentType != .radarr {
            Button(action: {
                if let id = item.id?.int64Value {
                    libraryViewModel.toggleItemSelection(id)
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
    
    private var selectionBottomBar: some View {
        HStack {
            if libraryViewModel.selectionCount == 1 {
                Button(action: {
                    selectedItemForEdit = libraryViewModel.selectedItem
                    showEditSheet = true
                }) {
                    Label(MR.strings().edit.localized(), systemImage: "pencil")
                }
                
                Spacer()
                
                Button(action: {
                    libraryViewModel.toggleMonitoringForSelected()
                }) {
                    let isMonitored = libraryViewModel.selectedItem?.monitored == true
                    Label(isMonitored ? MR.strings().monitored.localized() : MR.strings().unmonitored.localized(),
                          systemImage: isMonitored ? "bookmark.fill" : "bookmark")
                }
                
                Spacer()
            }
            
            Menu {
                Button(action: { libraryViewModel.refreshSelectedItems() }) {
                    Label(MR.strings().refresh.localized(), systemImage: "arrow.clockwise")
                }
                
                Button(action: { libraryViewModel.performAutomaticLookupSelected() }) {
                    Label(MR.strings().search_monitored.localized(), systemImage: "magnifyingglass")
                }
                
                if libraryViewModel.hasBazarr && (currentType == .sonarr || currentType == .radarr) {
                    Button(action: { libraryViewModel.performSubtitleSearchSelected() }) {
                        Label(MR.strings().bazarr_search_subtitles.localized(), systemImage: "captions.bubble")
                    }
                }
                
                if currentType != .radarr {
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
    
    @ViewBuilder
    private func editSheet(for item: ArrMedia) -> some View {
        let profiles = libraryViewModel.instanceData?.qualityProfiles ?? []
        let folders = libraryViewModel.instanceData?.rootFolders ?? []
        let tags = libraryViewModel.instanceData?.tags ?? []
        let isInProgress = libraryViewModel.editItemStatus is OperationStatusInProgress

        if let series = item as? ArrSeries {
            EditSeriesSheet(item: series, qualityProfiles: profiles, rootFolders: folders, tags: tags, editInProgress: isInProgress) { newItem, moveFiles in
                libraryViewModel.editItem(newItem, moveFiles: moveFiles)
            }
        } else if let movie = item as? ArrMovie {
            EditMovieSheet(item: movie, qualityProfiles: profiles, rootFolders: folders, tags: tags, editInProgress: isInProgress) { newItem, moveFiles in
                libraryViewModel.editItem(newItem, moveFiles: moveFiles)
            }
        } else if let artist = item as? Arrtist {
            EditArtistSheet(item: artist, qualityProfiles: profiles, rootFolders: folders, tags: tags, editInProgress: isInProgress) { newItem, moveFiles in
                libraryViewModel.editItem(newItem, moveFiles: moveFiles)
            }
        } else if let author = item as? Author {
            EditAuthorSheet(item: author, qualityProfiles: profiles, rootFolders: folders, tags: tags, editInProgress: isInProgress) { newItem, moveFiles in
                libraryViewModel.editItem(newItem, moveFiles: moveFiles)
            }
        } else if let audiobook = item as? Audiobook {
            EditAudiobookSheet(item: audiobook, qualityProfiles: profiles, rootFolders: folders, editInProgress: isInProgress) { newItem in
                libraryViewModel.editItem(newItem)
            }
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if libraryViewModel.arrInstances.isEmpty || selectedInstance == nil {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                    navigationManager.showLauncher = true
                } label: {
                    Image(systemName: "line.3.horizontal")
                }
            }
        } else if !libraryViewModel.isInSelectionMode {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                    navigationManager.showLauncher = true
                } label: {
                    Image(systemName: "line.3.horizontal")
                }
            }

            if uiState is ArrLibrarySuccess {
                toolbarViewOptions
            }
        } else {
            ToolbarItem(placement: .topBarLeading) {
                Button(MR.strings().close.localized()) {
                    libraryViewModel.exitSelectionMode()
                }
            }
            
            ToolbarItem(placement: .principal) {
                Text(MR.plurals().selected_count.localized(libraryViewModel.selectionCount))
                    .font(.headline)
            }
            
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: {
                    if libraryViewModel.areAllItemsSelected() {
                        libraryViewModel.clearSelection()
                    } else {
                        libraryViewModel.selectAllItems()
                    }
                }) {
                    Image(systemName: libraryViewModel.areAllItemsSelected() ? "checkmark.circle.fill" : "circle")
                }
            }
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarViewOptions: some ToolbarContent {
        ToolbarItemGroup(placement: .topBarTrailing) {
            Button(action: {
                navigationManager.go(to: .search(query: "", type: currentType, instanceId: selectedInstance?.id), of: currentType)
            }) {
                Image(systemName: "plus")
                    .imageScale(.medium)
            }
            
            Menu {
                Button(action: {
                    customizationSheetPresented = true
                }) {
                    Label(MR.strings().customization_options.localized(), systemImage: "paintpalette")
                }

                FilterByPickerMenu(
                    type: currentType,
                    filterBy: preferences.filterBy,
                    customFilters: libraryViewModel.instanceData?.customFilters ?? [],
                    selectedCustomFilterId: preferences.customFilterId?.int64Value,
                    changeFilterBy: { newValue in
                        libraryViewModel.updateFilterBy(newValue)
                    },
                    changeCustomFilter: { newValue in
                        libraryViewModel.updateCustomFilter(newValue)
                    })
                    .menuIndicator(.hidden)
                
                SortByPickerMenu(
                    type: currentType,
                    sortBy: preferences.sortBy,
                    sortOrder: preferences.sortOrder,
                    changeSortBy: { newValue in
                        libraryViewModel.updateSortBy(newValue)
                    },
                    changeSortOrder: { newValue in
                        libraryViewModel.updateSortOrder(newValue)
                    }
                )
                .menuIndicator(.hidden)
            } label: {
                Image(systemName: "line.3.horizontal.decrease")
            }
        }
    }
}
