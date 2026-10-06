//
//  ArrTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-03.
//

import Foundation
import SwiftUI
import Shared

struct ArrTab: View {
    private let type: InstanceType
    
    @ObservedObject private var arrMediaViewModel: ArrMediaViewModelS
    @StateObject private var instancesViewModel: InstancesViewModelS
    @StateObject private var networkViewModel: NetworkConnectivityViewModel = NetworkConnectivityViewModel()
    @StateObject private var globalPreferences = PreferencesViewModel()
    
    @EnvironmentObject private var navigation: NavigationManager
    
    @State private var searchPresented: Bool = false
    @State private var customizationSheetPresented: Bool = false
    
    private var uiState: ArrLibrary {
        arrMediaViewModel.uiState
    }
    
    private var instanceState: InstancesState {
        instancesViewModel.instancesState
    }
    
    private var preferences: InstancePreferences {
        arrMediaViewModel.preferences
    }
    
    
    init(type: InstanceType, types: [InstanceType]? = nil, viewModel: ArrMediaViewModelS) {
        self.type = type
        self.arrMediaViewModel = viewModel
        let resolvedTypes = types ?? [type]
        _instancesViewModel = StateObject(wrappedValue: InstancesViewModelS(types: resolvedTypes))
    }
    
    var body: some View {
        contentForState
            .navigationTitle(instanceState.selectedInstance?.label ?? type.name)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                toolbarContent
            }
            .refreshable {
                arrMediaViewModel.refresh()
            }
            .onReceive(instancesViewModel.$instancesState) { newState in
                if newState.selectedInstance != nil && uiState is ArrLibraryInitial {
                    arrMediaViewModel.refresh()
                }
            }
            .task {
                if instanceState.selectedInstance != nil && uiState is ArrLibraryInitial {
                    arrMediaViewModel.refresh()
                }
            }
            .sheet(isPresented: $customizationSheetPresented) {
                ArrViewCustomizationSheet(type: type, viewModel: arrMediaViewModel)
            }
    }
    
    @ViewBuilder
    private var contentForState: some View {
        if instanceState.selectedInstance == nil {
            VStack {
                NoInstanceView(type: type)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if uiState is ArrLibraryInitial {
            VStack {
                NoInstanceView(type: type)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if uiState is ArrLibraryLoading {
            ZStack {
                ProgressView()
                    .progressViewStyle(.circular)
            }
        } else if let success = uiState as? ArrLibrarySuccess {
            ArrLibraryView(type: type, viewModel: arrMediaViewModel, state: success, searchQuery: $arrMediaViewModel.searchQuery, searchPresented: $searchPresented)
        } else if let error = uiState as? ArrLibraryError {
            ZStack {
                ErrorView(
                    errorType: error.type,
                    message: error.message,
                    onOpenSettings: {
                        navigation.maybeEditInstance(of: type, instanceState.selectedInstance)
                    },
                    onRetry: { arrMediaViewModel.refresh() }
                )
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            VStack {
                NoInstanceView(type: type)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if !arrMediaViewModel.isInSelectionMode {
            if navigation.shouldShowDrawerButton(for: navigation.tabKey(for: type)) {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        navigation.showLauncher = true
                    } label: {
                        Image(systemName: "line.3.horizontal")
                    }
                }
            }

            if uiState is ArrLibrarySuccess {
                toolbarViewOptions
            }
            
            if !globalPreferences.hideInstanceSwitcher || instanceState.instances.count > 1 {
                ToolbarItem(placement: .topBarLeading) {
                    InstancePickerMenu(
                        instances: instanceState.instances,
                        selectedInstanceId: instanceState.selectedInstance?.id,
                        onChangeInstance: { instancesViewModel.setInstanceActive($0) },
                        onAddNewInstance: { navigation.goToNewInstance(of: instanceState.selectedInstance?.type ?? type) }
                    )
                    .menuIndicator(.hidden)
                }
            }
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarViewOptions: some ToolbarContent {
        let currentType = instanceState.selectedInstance?.type ?? type
        ToolbarItemGroup(placement: .topBarTrailing) {
            Button(action: {
                navigation.go(to: .search(query: "", type: currentType, instanceId: instanceState.selectedInstance?.id), of: currentType)
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
                    customFilters: arrMediaViewModel.instanceData?.customFilters ?? [],
                    selectedCustomFilterId: preferences.customFilterId?.int64Value,
                    changeFilterBy: { newValue in
                        arrMediaViewModel.updateFilterBy(newValue)
                    },
                    changeCustomFilter: { newValue in
                        arrMediaViewModel.updateCustomFilter(newValue)
                    })
                    .menuIndicator(.hidden)
                
                SortByPickerMenu(
                    type: currentType,
                    sortBy: preferences.sortBy,
                    sortOrder: preferences.sortOrder,
                    changeSortBy: { newValue in
                        arrMediaViewModel.updateSortBy(newValue)
                    },
                    changeSortOrder: { newValue in
                        arrMediaViewModel.updateSortOrder(newValue)
                    }
                )
                .menuIndicator(.hidden)
            } label: {
                Image(systemName: "line.3.horizontal.decrease")
            }

            if let instance = instanceState.selectedInstance {
                InstanceOptionsMenu(
                    instanceUrl: instance.url,
                    onRunRssSync: { arrMediaViewModel.runRssSync() },
                    onSearchAllMissing: { arrMediaViewModel.searchAllMissing() },
                    onSearchFiltered: { arrMediaViewModel.searchFiltered() },
                    onUpdateLibrary: { arrMediaViewModel.updateLibrary() },
                    onBackupDatabase: { arrMediaViewModel.backupDatabase() }
                ) {
                    instance.type.tabIcon.toImage(renderingMode: .template)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 20, height: 20)
                        .foregroundColor(instance.type.associatedColor.toSwiftUI())
                }
            }
        }
    }
    
    private var viewTypeToggle: some View {
        let viewType = preferences.viewType
        let newType: ViewType = viewType == .grid ? .list : .grid
        
        return Button(action: {
            arrMediaViewModel.updateViewType(newType)
        }) {
            Label(preferences.viewType.name, systemImage: viewType == .grid ? "rectangle.grid.2x2" : "rectangle.grid.1x2")
        }
    }
    
    @ViewBuilder
    private func errorView() -> some View {
        VStack(alignment: .center, spacing: 8) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 64))
                .imageScale(.large)
            
            Text(MR.strings().couldnt_connect.localized())
                .font(.system(size: 20, weight: .medium))
                .multilineTextAlignment(.center)
            Text(MR.strings().couldnt_connect_message.localized())
                .multilineTextAlignment(.center)
            Button(action: {
                arrMediaViewModel.refresh()
            }) {
                Text(MR.strings().retry.localized())
            }
        }
        .padding(.horizontal, 24)
    }
    
}
