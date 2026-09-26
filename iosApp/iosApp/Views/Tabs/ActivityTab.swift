//
//  ActivityTab.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-21.
//

import SwiftUI
import Shared

struct ActivityTab: View {
    var body: some View {
        ActivityTabContent()
    }
}

struct ActivityTabContent: View {
    
    @StateObject private var viewModel = ActivityQueueViewModelS()
    @EnvironmentObject private var navigationManager: NavigationManager
    
    @State private var selectedItem: IdentifiableQueueItem? = nil
    
    private var titleText: String {
        guard !viewModel.queueItems.isEmpty else { return MR.strings().activity.localized() }
        return "\(MR.strings().activity.localized()) (\(viewModel.queueItems.count))"
    }
    
    var body: some View {
        queueItemContent
            .navigationTitle(titleText)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                if navigationManager.shouldShowDrawerButton(for: TabItemStandard.activity.key) {
                    ToolbarItem(placement: .topBarLeading) {
                        Button {
                            navigationManager.showLauncher = true
                        } label: {
                            Image(systemName: "line.3.horizontal")
                        }
                    }
                }

                ToolbarItem(placement: .primaryAction) {
                    ActivityFilterMenu(
                        sortBy: Binding(
                            get: { viewModel.uiState.sortBy },
                            set: { viewModel.setSortBy($0) }
                        ),
                        sortOrder: Binding(
                            get: { viewModel.uiState.sortOrder },
                            set: { viewModel.setSortOrder($0) }
                        ),
                        instanceId: Binding(
                            get: { viewModel.uiState.instanceId?.int64Value },
                            set: { viewModel.setInstanceId($0) }
                        ),
                        instances: viewModel.instances
                    )
                }
            }
            .sheet(item: $selectedItem) { wrapper in
                QueueItemInfoSheet(item: wrapper.item, deleteInProgress: viewModel.removeInProgress, onDelete: { remove, block, skip in
                    viewModel.removeQueueItem(wrapper.item, remove, block, skip)
                })
                .presentationDetents([.medium, .large])
                .presentationDragIndicator(.visible)
            }
            .refreshable {
                viewModel.refresh()
            }
            .onChange(of: viewModel.removeSuccesss) { _, newValue in
                if newValue {
                    selectedItem = nil
                    viewModel.refresh()
                }
            }
    }
    
    @ViewBuilder
    private var queueItemContent: some View {
        if viewModel.queueItems.isEmpty {
            ContentUnavailableView(
                MR.strings().no_activity.localized(),
                systemImage: "square.and.arrow.down.fill"
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            List {
                ForEach(viewModel.queueItems, id: \.id) { item in
                    ActivityQueueItem(
                        item: item,
                        useFullColorCards: viewModel.useColoredCards,
                        onClick: {
                            selectedItem = IdentifiableQueueItem(item: item)
                        }
                    )
                    .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)
                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                        Button(role: .destructive) {
                            selectedItem = IdentifiableQueueItem(item: item)
                        } label: {
                            Label(MR.strings().delete.localized(), systemImage: "trash.fill")
                        }
                    }
                }
            }
            .listStyle(.plain)
        }
    }
}
