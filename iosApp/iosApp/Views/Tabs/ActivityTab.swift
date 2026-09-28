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
        switch viewModel.uiState.selectedTab {
        case .activity:
            guard !viewModel.queueItems.isEmpty else { return MR.strings().activity.localized() }
            return "\(MR.strings().activity.localized()) (\(viewModel.queueItems.count))"
        case .history:
            guard !viewModel.historyItems.isEmpty else { return MR.strings().history.localized() }
            return "\(MR.strings().history.localized()) (\(viewModel.historyItems.count))"
        case .downloaded:
            guard !viewModel.downloadedItems.isEmpty else { return MR.strings().recently_downloaded.localized() }
            return "\(MR.strings().recently_downloaded.localized()) (\(viewModel.downloadedItems.count))"
        default:
            return MR.strings().activity.localized()
        }
    }
    
    var body: some View {
        VStack(spacing: 0) {
            Picker("", selection: Binding(
                get: { viewModel.uiState.selectedTab },
                set: { viewModel.setSelectedTab($0) }
            )) {
                ForEach(ActivityTabSegment.allCases, id: \.self) { segment in
                    Text(segment.resource.localized()).tag(segment)
                }
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            
            tabContent
        }
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
                    selectedTab: viewModel.uiState.selectedTab,
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
                    historyInstanceId: Binding(
                        get: { viewModel.uiState.historyInstanceId?.int64Value },
                        set: { viewModel.setHistoryInstanceId($0) }
                    ),
                    historyStateFilter: Binding(
                        get: { viewModel.uiState.historyStateFilter },
                        set: { viewModel.setHistoryStateFilter($0) }
                    ),
                    downloadedInstanceId: Binding(
                        get: { viewModel.uiState.downloadedInstanceId?.int64Value },
                        set: { viewModel.setDownloadedInstanceId($0) }
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
    private var tabContent: some View {
        switch viewModel.uiState.selectedTab {
        case .activity:
            queueItemContent
        case .history:
            historyContent
        case .downloaded:
            downloadedContent
        default:
            queueItemContent
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
    
    @ViewBuilder
    private var historyContent: some View {
        if viewModel.historyItems.isEmpty {
            ContentUnavailableView(
                MR.strings().no_history.localized(),
                systemImage: "clock.arrow.circlepath"
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            ScrollViewReader { proxy in
                List {
                    ForEach(Array(viewModel.historyItems.enumerated()), id: \.offset) { index, item in
                        HistoryItemView(item: item)
                            .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                            .listRowSeparator(.hidden)
                            .listRowBackground(Color.clear)
                            .id(index)
                    }
                }
                .listStyle(.plain)
                .onChange(of: viewModel.uiState.historyStateFilter) { _, _ in
                    var transaction = Transaction()
                    transaction.disablesAnimations = true
                    withTransaction(transaction) {
                        proxy.scrollTo(0, anchor: .top)
                    }
                }
                .onChange(of: viewModel.uiState.historyInstanceId) { _, _ in
                    var transaction = Transaction()
                    transaction.disablesAnimations = true
                    withTransaction(transaction) {
                        proxy.scrollTo(0, anchor: .top)
                    }
                }
            }
        }
    }

    @ViewBuilder
    private var downloadedContent: some View {
        if viewModel.downloadedItems.isEmpty {
            ContentUnavailableView(
                MR.strings().no_downloaded_media.localized(),
                systemImage: "arrow.down.circle"
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            ScrollViewReader { proxy in
                List {
                    ForEach(Array(viewModel.downloadedItems.enumerated()), id: \.offset) { index, item in
                        DownloadedMediaItemView(item: item)
                            .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                            .listRowSeparator(.hidden)
                            .listRowBackground(Color.clear)
                            .id(index)
                    }
                }
                .listStyle(.plain)
                .onChange(of: viewModel.uiState.downloadedInstanceId) { _, _ in
                    var transaction = Transaction()
                    transaction.disablesAnimations = true
                    withTransaction(transaction) {
                        proxy.scrollTo(0, anchor: .top)
                    }
                }
            }
        }
    }
}

