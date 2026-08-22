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
    @Environment(\.navigationContext) private var context
    @EnvironmentObject private var navigationManager: NavigationManager
    @StateObject private var libraryViewModel = UnifiedLibraryViewModelS()
    
    var body: some View {
        switch context {
        case .mainTab:
            NavigationStack(path: $navigationManager.libraryPath) {
                LibraryTabContent(libraryViewModel: libraryViewModel)
            }
        case .launcher:
            LibraryTabContent(libraryViewModel: libraryViewModel)
        }
    }
}

struct LibraryTabContent: View {
    @ObservedObject var libraryViewModel: UnifiedLibraryViewModelS
    @EnvironmentObject private var navigationManager: NavigationManager
    
    @ObservedObject private var activityQueueViewModel = ActivityQueueViewModelS()
    
    @State private var searchPresented: Bool = false
    @State private var customizationSheetPresented: Bool = false
    @State private var confirmDelete: Bool = false
    @State private var showEditSheet: ArrMedia? = nil
    @State private var moveFilesItem: ArrMedia? = nil
    @State private var confirmBulkDelete: Bool = false
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
        Group {
            if libraryViewModel.arrInstances.isEmpty || selectedInstance == nil {
                VStack {
                    NoInstanceView(type: .sonarr)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .navigationTitle(MR.strings().library.localized())
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .topBarLeading) {
                        Button {
                            navigationManager.showLauncher = true
                        } label: {
                            Image(systemName: "line.3.horizontal")
                        }
                    }
                }
            } else if let instance = selectedInstance {
                VStack(spacing: 0) {
                    if libraryViewModel.arrInstances.count > 1 {
                        topTabsRow
                    }
                    
                    contentForState(instance: instance)
                }
                .navigationTitle(instance.label)
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
                        preferences: preferences,
                        changeViewType: { libraryViewModel.updateViewType($0) },
                        changeShowFullDetails: { libraryViewModel.updateShowFullDetails($0) },
                        changeShowOverlay: { libraryViewModel.updateShowOverlay($0) },
                        changeShowBannerBackground: { libraryViewModel.updateShowBannerBackground($0) },
                        changeIncludeOverview: { libraryViewModel.updateIncludeOverview($0) },
                        changeBannerBlur: { libraryViewModel.updateBannerBlur($0) },
                        changeGridDensity: { libraryViewModel.updateGridDensity($0) },
                        changeGridSpacing: { libraryViewModel.updateGridSpacing($0) },
                        changePosterElevation: { libraryViewModel.updatePosterElevation($0) },
                        changePosterRadius: { libraryViewModel.updatePosterRadius($0) },
                        changeApplyGlobally: { libraryViewModel.updateApplyGlobally($0) }
                    )
                }
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
                    Button {
                        libraryViewModel.selectInstance(tabInstance)
                    } label: {
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
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(isSelected ? Color.accentColor : Color(UIColor.secondarySystemBackground))
                        .foregroundColor(isSelected ? .white : .primary)
                        .clipShape(Capsule())
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal)
            .padding(.vertical, 8)
        }
        .background(Color(UIColor.systemBackground))
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
                        mediaListOrGrid(items: success.items, prefs: success.preferences)
                    }
                }
                
                if libraryViewModel.isInSelectionMode {
                    selectionBottomBar
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                        .zIndex(1)
                }
            }
            .searchable(
                text: $libraryViewModel.searchQuery,
                isPresented: $searchPresented,
                placement: .navigationBarDrawer(displayMode: .automatic)
            )
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
    
    @ViewBuilder
    private func mediaListOrGrid(items: [ArrMedia], prefs: InstancePreferences) -> some View {
        MediaGridList(
            type: currentType,
            items: items,
            onItemTapped: { item in
                navigationManager.go(to: .details(media: item, type: currentType), of: currentType)
            },
            preferences: prefs,
            itemIsActive: { item in
                activityQueueViewModel.queueItems.contains(where: { $0.mediaId == item.id })
            },
            multiSelectEnabled: libraryViewModel.isInSelectionMode,
            selectedItems: libraryViewModel.selectedItems,
            onToggleItemSelection: { id in
                if libraryViewModel.selectedItems.contains(id) {
                    libraryViewModel.clearSelection()
                }
            }
        )
    }
    
    private var selectionBottomBar: some View {
        SelectionBottomBar(
            type: currentType,
            selectedCount: libraryViewModel.selectionCount,
            isMonitored: libraryViewModel.selectedItem?.monitored == true,
            hasBazarr: libraryViewModel.hasBazarr,
            onEdit: {
                showEditSheet = libraryViewModel.selectedItem
            },
            onDelete: {
                confirmBulkDelete = true
            },
            onToggleMonitor: {
                libraryViewModel.toggleMonitoringForSelected()
            },
            onRefresh: {
                libraryViewModel.refreshSelectedItems()
            },
            onAutomaticSearch: {
                libraryViewModel.performAutomaticLookupSelected()
            },
            onSubtitleSearch: {
                libraryViewModel.performSubtitleSearchSelected()
            },
            onShowMonitorOptions: {
                showMonitorOptions = true
            }
        )
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if !libraryViewModel.isInSelectionMode {
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
                Button(MR.strings().cancel.localized()) {
                    libraryViewModel.exitSelectionMode()
                }
            }
            ToolbarItem(placement: .topBarTrailing) {
                Button(libraryViewModel.areAllItemsSelected() ? MR.strings().deselect_all.localized() : MR.strings().select_all.localized()) {
                    if libraryViewModel.areAllItemsSelected() {
                        libraryViewModel.clearSelection()
                    } else {
                        libraryViewModel.selectAllItems()
                    }
                }
            }
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarViewOptions: some ToolbarContent {
        ToolbarItemGroup(placement: .topBarTrailing) {
            Button(action: {
                navigationManager.go(to: .search(query: "", type: currentType), of: currentType)
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
