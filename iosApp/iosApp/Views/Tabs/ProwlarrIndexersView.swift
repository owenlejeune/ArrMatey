//
//  ProwlarrIndexersView.swift
//  iosApp
//
//  Created by Bryan Moon on 2026-03-04.
//

import SwiftUI
import Shared

struct ProwlarrIndexersView: View {
    @ObservedObject var viewModel: ProwlarrIndexersViewModelS
    @ObservedObject private var instanceViewModel = InstancesViewModelS(type: .prowlarr)
    
    @State private var selectedItem: IdentifiableIndexerStatus? = nil
    
    var body: some View {
        VStack(spacing: 0) {
            indexersContent
        }
    }
    
    @ViewBuilder
    private var indexersContent: some View {
        Group {
            if instanceViewModel.instancesState.selectedInstance == nil {
                NoInstanceView(type: .prowlarr)
            } else if viewModel.indexers is ProwlarrIndexersStateLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if viewModel.indexers is ProwlarrIndexersStateError {
                if let error = viewModel.indexers as? ProwlarrIndexersStateError {
                    errorView(message: error.message)
                }
            } else if viewModel.indexers is ProwlarrIndexersStateSuccess {
                if let success = viewModel.indexers as? ProwlarrIndexersStateSuccess {
                    if success.items.isEmpty {
                        emptyView
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        ScrollView {
                            VStack(spacing: 12) {
                                ForEach(Array(success.items.enumerated()), id: \.offset) { _, indexer in
                                    IndexerRow(
                                        indexer: indexer,
                                        hasIssues: viewModel.indexersStatus.first(where: { $0.indexerId == indexer.id })?.hasFailure ?? false,
                                        onShowIssues: {
                                            if let status = viewModel.indexersStatus.first(where: { $0.indexerId == indexer.id }) {
                                                selectedItem = IdentifiableIndexerStatus(item: status)
                                            }
                                        }
                                    )
                                }
                            }
                            .padding(.vertical, 12)
                            .padding(.horizontal, 16)
                        }
                    }
                }
            } else {
                // Initial state
                initialView
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .sheet(item: $selectedItem) { wrapper in
            IndexerStatusSheet(indexerStatus: wrapper.item)
                .presentationDragIndicator(.visible)
                .presentationDetents([.fraction(0.33)])
        }
    }
    
    @ViewBuilder
    private var emptyView: some View {
        VStack(spacing: 12) {
            Image(systemName: "list.bullet.rectangle")
                .font(.system(size: 64))
                .foregroundStyle(.secondary)
            Text(MR.strings().no_indexers_configured.localized())
                .font(.system(size: 17))
                .foregroundStyle(.secondary)
        }
    }
    
    @ViewBuilder
    private var initialView: some View {
        VStack(spacing: 12) {
            ProgressView()
            Text(MR.strings().loading_indexers.localized())
                .font(.caption)
                .foregroundStyle(.secondary)
        }
    }
    
    private func errorView(message: String) -> some View {
        VStack(spacing: 12) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 48))
                .foregroundStyle(.red)
            Text(message)
                .font(.system(size: 15))
                .foregroundStyle(.red)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct IndexerRow: View {
    let indexer: ProwlarrIndexer
    let hasIssues: Bool
    let onShowIssues: () -> Void
    
    var body: some View {
        Button(action: {
            if hasIssues {
                onShowIssues()
            }
        }) {
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text(indexer.name ?? indexer.implementationName ?? MR.strings().unknown.localized())
                        .font(.system(size: 16, weight: .semibold))
                        .lineLimit(1)
                    
                    Spacer()
                    
                    let protocolText = indexer.protocol?.name ?? MR.strings().unknown.localized()
                    Text(protocolText)
                        .font(.caption)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(protocolColor(for: protocolText))
                        .foregroundColor(.white)
                        .cornerRadius(4)
                }
                
                HStack(spacing: 12) {
                    // Enable status
                    HStack(spacing: 4) {
                        Circle()
                            .fill(indexer.enable ? Color.green : Color.gray)
                            .frame(width: 8, height: 8)
                        Text(indexer.enable
                             ? MR.strings().enabled.localized()
                             : MR.strings().disabled.localized())
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    }
                    
                    // RSS support
                    if indexer.supportsRss {
                        HStack(spacing: 2) {
                            Image(systemName: "dot.radiowaves.left.and.right")
                                .font(.caption)
                            Text("RSS")
                                .font(.caption)
                        }
                        .foregroundStyle(.secondary)
                    }
                    
                    // Search support
                    if indexer.supportsSearch {
                        HStack(spacing: 2) {
                            Image(systemName: "magnifyingglass")
                                .font(.caption)
                            Text(MR.strings().search.localized())
                                .font(.caption)
                        }
                        .foregroundStyle(.secondary)
                    }
                    
                    Spacer()
                    
                    if hasIssues {
                        Image(systemName: "exclamationmark.triangle")
                            .frame(width: 18, height: 18)
                            .foregroundColor(.arrOrange)
                    }
                    
                }
                
                if let msg = indexer.message?.message, !msg.isEmpty {
                    Text(msg)
                        .font(.caption)
                        .foregroundStyle(indexer.message?.type == .warning ? Color.orange : Color.red)
                        .lineLimit(2)
                }
            }
            .padding(12)
            .background(Color(.systemGray6))
            .cornerRadius(8)
        }
    }
    
    private func protocolColor(for proto: String) -> Color {
        switch proto {
        case "torrent": return .blue
        case "usenet": return .green
        default: return .gray
        }
    }
}
