//
//  DownloadsTab.swift
//  iosApp
//

import SwiftUI
import Shared

struct DownloadsTab: View {

    @StateObject private var viewModel = DownloadQueueViewModelS()
    @StateObject private var clientsViewModel = DownloadClientsViewModelS()
    @EnvironmentObject private var navigation: NavigationManager

    @State private var deleteTarget: DownloadItem? = nil
    @State private var deleteId: String? = nil
    @State private var showDeleteConfirm: Bool = false
    @State private var searchQuery: String = ""
    @State private var toastMessage: String? = nil
    
    private var searchPrompt: String {
        let count = viewModel.downloadQueueState.queueItems.count
        return MR.plurals().search_downloads.localized(count)
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            Group {
                if clientsViewModel.downloadClientsState.downloadClients.isEmpty {
                    NoDownloadClientsView()
                } else if !viewModel.hasLoaded {
                    ProgressView()
                        .scaleEffect(2)
                } else {
                    queueContent
                }
            }

            if viewModel.isInSelectionMode {
                selectionBottomBar
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .zIndex(1)
            }
        }
        .navigationTitle(MR.strings().downloads.localized())
        .toolbar {
            toolbarContent
        }
        .onChange(of: viewModel.isCommandSuccess) { _, isSuccess in
            if isSuccess {
                deleteTarget = nil
                viewModel.resetCommandState()
            }
        }
        .onChange(of: viewModel.isCommandError) { _, isError in
            if isError {
                let error = viewModel.commandState as? DownloadClientCommandStateError
                toastMessage = error?.message ?? MR.strings().error.localized()
                deleteTarget = nil
                deleteId = nil
                viewModel.resetCommandState()
            }
        }
        .overlay(alignment: .bottom) {
            if let message = toastMessage {
                ToastView(message: message)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                            withAnimation { toastMessage = nil }
                        }
                    }
            }
        }
        .animation(.easeInOut(duration: 0.3), value: toastMessage != nil)
        .confirmationDialog(
            MR.strings().delete_files.localized(),
            isPresented: $showDeleteConfirm,
            titleVisibility: .visible
        ) {
            if let id = deleteId {
                Button(MR.strings().yes.localized(), role: .destructive) {
                    viewModel.deleteDownload(id, deleteFiles: true)
                }
                Button(MR.strings().no.localized()) {
                    viewModel.deleteDownload(id, deleteFiles: false)
                }
            } else if viewModel.isInSelectionMode {
                Button(MR.strings().yes.localized(), role: .destructive) {
                    viewModel.deleteSelected(deleteFiles: true)
                }
                Button(MR.strings().no.localized()) {
                    viewModel.deleteSelected(deleteFiles: false)
                }
            }
            Button(MR.strings().cancel.localized(), role: .cancel) {
                deleteTarget = nil
                deleteId = nil
            }
        } message: {
            if viewModel.isInSelectionMode {
                Text(MR.plurals().selected_count.localized(viewModel.selectionCount))
            } else {
                Text(deleteTarget?.name ?? "")
            }
        }
    }

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if viewModel.isInSelectionMode {
            ToolbarItem(placement: .topBarLeading) {
                Button(MR.strings().close.localized()) {
                    viewModel.exitSelectionMode()
                }
            }
            
            ToolbarItem(placement: .principal) {
                Text(MR.plurals().selected_count.localized(viewModel.selectionCount))
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
        } else {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                    navigation.showLauncher = true
                } label: {
                    Image(systemName: "line.3.horizontal")
                }
            }

            ToolbarItem(placement: .primaryAction) {
                DownloadQueueFilterMenu(
                    filterState: viewModel.filterState,
                    sortBy: Binding(
                        get: { viewModel.sortState.sortBy },
                        set: { viewModel.updateSortBy($0) }
                    ),
                    sortOrder: Binding(
                        get: { viewModel.sortState.sortOrder },
                        set: { viewModel.updateSortOrder($0) }
                    ),
                    availableTags: viewModel.downloadQueueState.queueItems.flatMap { $0.tags }.unique().sorted(),
                    onToggleStatus: { viewModel.toggleStatusFilter(status: $0) },
                    onToggleTag: { viewModel.toggleTagFilter(tag: $0) },
                    onUpdateActiveOnly: { viewModel.updateActiveOnly(activeOnly: $0) },
                    onUpdateCompletedOnly: { viewModel.updateCompletedOnly(completedOnly: $0) },
                    onUpdateExcludeStatuses: { viewModel.updateExcludeStatuses(exclude: $0) },
                    onUpdateExcludeTags: { viewModel.updateExcludeTags(exclude: $0) },
                    onClearFilters: { viewModel.clearFilters() }
                )
            }
        }
    }

    private var selectionBottomBar: some View {
        HStack {
            Button(action: { viewModel.pauseSelected() }) {
                Label(MR.strings().pause.localized(), systemImage: "pause.fill")
            }
            
            Spacer()
            
            Button(action: { viewModel.resumeSelected() }) {
                Label(MR.strings().resume.localized(), systemImage: "play.fill")
            }
            
            Spacer()
            
            Button(role: .destructive, action: {
                showDeleteConfirm = true
            }) {
                Label(MR.strings().delete.localized(), systemImage: "trash")
            }
            .foregroundStyle(.red)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .floatingCapsuleBackground(material: .regularMaterial)
        .padding(.horizontal, 16)
        .padding(.bottom, 20)
    }

    @ViewBuilder
    private var queueContent: some View {
        VStack(spacing: 0) {
            ClientFilterRow(
                clients: clientsViewModel.downloadClientsState.downloadClients,
                transferInfos: viewModel.downloadQueueState.transferInfo,
                selectedIds: viewModel.filterState.clientIds as? [Int64] ?? [],
                onToggle: {
                    if clientsViewModel.downloadClientsState.downloadClients.count > 1 {
                        viewModel.toggleClientIdFilter(id: $0)
                    }
                }
            )
            .padding(.top, 8)
            .padding(.bottom, 4)
            
            if viewModel.downloadQueueState.queueItems.isEmpty {
                emptyView
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List {
                    ForEach(viewModel.downloadQueueState.queueItems, id: \.id) { item in
                        let isSelected = viewModel.selectedItems.contains(item.id)
                        HStack {
                            if viewModel.isInSelectionMode {
                                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                                    .foregroundColor(isSelected ? .blue : .secondary)
                                    .onTapGesture {
                                        viewModel.toggleItemSelection(item.id)
                                    }
                            }
                            
                            DownloadQueueItemView(
                                item: item,
                                showClientInfo: clientsViewModel.downloadClientsState.downloadClients.count > 1
                            )
                            .onTapGesture {
                                if viewModel.isInSelectionMode {
                                    viewModel.toggleItemSelection(item.id)
                                }
                            }
                            .onLongPressGesture {
                                viewModel.toggleItemSelection(item.id)
                                viewModel.enterSelectionMode()
                            }
                        }
                        .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                        .listRowSeparator(.hidden)
                        .listRowBackground(Color.clear)
                        .swipeActions(edge: .leading, allowsFullSwipe: false) {
                            if !viewModel.isInSelectionMode {
                                Button {
                                    if item.status.isPaused {
                                        viewModel.resumeDownload(item.id)
                                    } else {
                                        viewModel.pauseDownload(item.id)
                                    }
                                } label: {
                                    Label(item.status.isPaused ? "Resume" : "Pause", systemImage: item.status.isPaused ? "play.fill" : "pause.fill")
                                }
                                .tint(.blue)
                            }
                        }
                        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                            if !viewModel.isInSelectionMode {
                                Button(role: .destructive) {
                                    deleteTarget = item
                                    deleteId = item.id
                                    showDeleteConfirm = true
                                } label: {
                                    Label("Delete", systemImage: "trash.fill")
                                }
                            }
                        }
                    }
                }
                .listStyle(.plain)
                .refreshable {
                    viewModel.refresh()
                }
            }
        }
        .searchable(
            text: $searchQuery,
            prompt: searchPrompt
        )
        .onChange(of: searchQuery) { _, query in
            viewModel.updateSearchQuery(query)
        }
    }

    @ViewBuilder
    private var emptyView: some View {
        if let error = viewModel.errorMessage {
            ContentUnavailableView {
                Label(MR.strings().error.localized(), systemImage: "exclamationmark.triangle")
                    .foregroundStyle(.red)
            } description: {
                Text(error)
            }
        } else if viewModel.isRefreshing {
            ProgressView()
                .scaleEffect(1.5)
        } else {
            ContentUnavailableView(
                MR.strings().no_activity.localized(),
                systemImage: "arrow.down.circle"
            )
        }
    }
}

extension Array where Element: Hashable {
    func unique() -> [Element] {
        var seen = Set<Element>()
        return filter { seen.insert($0).inserted }
    }
}

struct NoDownloadClientsView: View {
    @EnvironmentObject private var navigation: NavigationManager
    
    var body: some View {
        ContentUnavailableView {
            Label(
                MR.strings().no_download_clients.localized(),
                systemImage: "cloud.rainbow.half"
            )
        } actions: {
            Button {
                navigation.go(to: .newDownloadClient)
            } label: {
                Label(MR.strings().add_instance.localized(), systemImage: "plus.circle.fill")
            }
            .buttonStyle(.borderedProminent)
            .padding(.top, 4)
        }
    }
}

struct ClientFilterRow: View {
    let clients: [DownloadClient]
    let transferInfos: [DownloadTransferInfo]
    let selectedIds: [Int64]
    let onToggle: (Int64) -> Void
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(clients, id: \.id) { client in
                    ClientFilterChip(client: client, info: transferInfos.first(where: { $0.client.id == client.id }), isSelected: selectedIds.contains(client.id), onClick: { onToggle(client.id) })
                }
            }
            .padding(.horizontal, 16)
        }
    }
}

struct ClientFilterChip: View {
    let client: DownloadClient
    let info: DownloadTransferInfo?
    let isSelected: Bool
    let onClick: () -> Void
    
    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 6) {
                client.type.icon.toImage(renderingMode: .original)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: 16, height: 16)
                
                Text("↓ \((info?.downloadSpeed ?? 0).bytesAsFileSizeString())/s ↑ \((info?.uploadSpeed ?? 0).bytesAsFileSizeString())/s")
                    .font(.subheadline)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(isSelected ? Color.accentColor : Color(.secondarySystemBackground))
            .foregroundColor(isSelected ? .white : .primary)
            .clipShape(Capsule())
            .overlay(
                Capsule()
                    .stroke(Color.primary.opacity(0.1), lineWidth: isSelected ? 0 : 1)
            )
        }
        .buttonStyle(.plain)
    }
}
