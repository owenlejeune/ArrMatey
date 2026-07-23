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
    
    private var searchPrompt: String {
        let count = viewModel.downloadQueueState.queueItems.count
        return MR.strings().search_downloads.formatted(args: [count])
    }

    var body: some View {
        ZStack {
            if clientsViewModel.downloadClientsState.downloadClients.isEmpty {
                NoDownloadClientsView()
            } else if !viewModel.hasLoaded {
                ProgressView()
                    .scaleEffect(2)
            } else {
                queueContent
            }
        }
        .navigationTitle(MR.strings().downloads.localized())
        .toolbar {
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
        .onChange(of: viewModel.isCommandSuccess) { _, isSuccess in
            if isSuccess {
                deleteTarget = nil
                viewModel.resetCommandState()
            }
        }
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
            }
            Button(MR.strings().cancel.localized(), role: .cancel) {
                deleteTarget = nil
                deleteId = nil
            }
        } message: {
            Text(deleteTarget?.name ?? "")
        }
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
                        DownloadQueueItemView(
                            item: item,
                            showClientInfo: viewModel.filterState.clientIds.count > 1
                        )
                        .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                        .listRowSeparator(.hidden)
                        .swipeActions(edge: .leading, allowsFullSwipe: false) {
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
                        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
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
        VStack(alignment: .center, spacing: 12) {
            Image(systemName: "arrow.down.circle")
                .font(.system(size: 64))
                .foregroundStyle(.secondary)
            Text(MR.strings().no_activity.localized())
                .font(.system(size: 20, weight: .bold))
        }
        .padding(.horizontal, 24)
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
        VStack(spacing: 16) {
            Image(systemName: "cloud.rainbow.half")
                .font(.system(size: 100))
                .foregroundStyle(.secondary)
            
            Text(MR.strings().no_download_clients.localized())
                .font(.title2)
                .fontWeight(.medium)
                .multilineTextAlignment(.center)
            
            Button(action: {
                navigation.go(to: .newDownloadClient)
            }) {
                HStack {
                    Image(systemName: "plus.circle.fill")
                    Text(MR.strings().add_instance.localized())
                        .fontWeight(.medium)
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 12)
                .background(Color.accentColor)
                .foregroundColor(.white)
                .cornerRadius(10)
            }
        }
        .padding(32)
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
