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
    @ObservedObject private var instancesViewModel: InstancesViewModelS
    @ObservedObject private var activityQueueViewModel: ActivityQueueViewModelS = ActivityQueueViewModelS()
    @ObservedObject private var networkViewModel: NetworkConnectivityViewModel = NetworkConnectivityViewModel()
    
    @EnvironmentObject private var navigation: NavigationManager
    
    @State private var searchPresented: Bool = false
    
    private var uiState: ArrLibrary {
        arrMediaViewModel.uiState
    }
    
    private var instanceState: InstancesState {
        instancesViewModel.instancesState
    }
    
    private var queueItems: [QueueItem] {
        activityQueueViewModel.queueItems
    }
    
    private var preferences: InstancePreferences {
        arrMediaViewModel.preferences
    }
    
    
    init(type: InstanceType, viewModel: ArrMediaViewModelS) {
        self.type = type
        self.arrMediaViewModel = viewModel
        self.instancesViewModel = InstancesViewModelS(type: type)
    }
    
    var body: some View {
        contentForState
            .navigationTitle(instanceState.selectedInstance?.label ?? type.name)
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
            ArrLibraryView(type: type, state: success, searchQuery: $arrMediaViewModel.searchQuery, searchPresented: $searchPresented)
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
        if uiState is ArrLibrarySuccess {
            toolbarViewOptions
        }
        
        ToolbarItem(placement: .topBarLeading) {
            InstancePickerMenu(
                instances: instanceState.instances,
                onChangeInstance: { instancesViewModel.setInstanceActive($0) },
                onAddNewInstance: { navigation.goToNewInstance(of: type) }
            )
            .menuIndicator(.hidden)
        }
    }
    
    @ToolbarContentBuilder
    private var toolbarViewOptions: some ToolbarContent {
        ToolbarItemGroup(placement: .topBarTrailing) {
            Button(action: {
                navigation.go(to: .search(query: "", type: type), of: type)
            }) {
                Image(systemName: "plus")
                    .imageScale(.medium)
            }
            
            Menu {
                viewTypeToggle
                
                FilterByPickerMenu(
                    type: type,
                    filterBy: preferences.filterBy,
                    changeFilterBy: { newValue in
                        arrMediaViewModel.updateFilterBy(newValue)
                    })
                    .menuIndicator(.hidden)
                
                SortByPickerMenu(
                    type: type,
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
