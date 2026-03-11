//
//  DownloadsTab.swift
//  iosApp
//

import SwiftUI
import Shared

struct DownloadsTab: View {

    @ObservedObject private var viewModel = DownloadQueueViewModelS()
    @ObservedObject private var clientsViewModel = DownloadClientsViewModelS()

    @State private var deleteTarget: DownloadItem? = nil
    @State private var deleteId: String? = nil
    @State private var showDeleteConfirm: Bool = false
    @State private var searchQuery: String = ""
    
    private var searchPrompt: String {
        let count = viewModel.downloadQueueState.queueItems.count
        return MR.strings().search_downloads.formatted(args: [count])
    }

    var body: some View {
        queueContent
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    DownloadClientQueueSortMenu(
                        sortBy: Binding(
                            get: { viewModel.sortState.sortBy },
                            set: { viewModel.updateSortBy($0) }
                        ),
                        sortOrder: Binding(
                            get: { viewModel.sortState.sortOrder },
                            set: { viewModel.updateSortOrder($0) }
                        )
                    )
                }
            }
            .navigationTitle(MR.strings().downloads.localized())
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
        VStack(spacing: 12) {
            ClientFilterRow(
                clients: clientsViewModel.downloadClientsState.downloadClients,
                transferInfos: viewModel.downloadQueueState.transferInfo,
                selectedIds: viewModel.clientIdsFilters,
                onToggle: {
                    if clientsViewModel.downloadClientsState.downloadClients.count > 1 {
                        viewModel.toggleClientIdFilter(id: $0)
                    }
                }
            )
            
            if viewModel.downloadQueueState.queueItems.isEmpty {
                emptyView
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                ScrollView {
                    VStack(spacing: 12) {
                        ForEach(viewModel.downloadQueueState.queueItems, id: \.self) { item in
                            downloadItemRow(item)
                        }
                    }
                    .padding(.vertical, 12)
                    .padding(.horizontal, 18)
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
    private func downloadItemRow(_ item: DownloadItem) -> some View {
        DownloadQueueItemView(
            item: item,
            actionInProgress: viewModel.isCommandLoading,
            onPause: { viewModel.pauseDownload(item.id) },
            onResume: { viewModel.resumeDownload(item.id) },
            onDelete: {
                deleteTarget = item
                deleteId = item.id
                showDeleteConfirm = true
            },
            showClientInfo: viewModel.clientIdsFilters.count > 1
        )
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
            .padding(.horizontal, 18)
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
