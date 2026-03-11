//
//  ProwlarrSearchView.swift
//  iosApp
//
//  Created by Bryan Moon on 2026-03-04.
//

import SwiftUI
import Shared

struct ProwlarrSearchView: View {
    @ObservedObject private var viewModel = ProwlarrSearchViewModelS()
    @State private var queryText = ""
    @State private var grabTarget: ProwlarrSearchResult? = nil
    @State private var grabbingGuid: String? = nil
    @State private var toastMessage: String? = nil
    
    var body: some View {
        VStack(spacing: 0) {
            searchContent
        }
        .searchable(text: $queryText, prompt: MR.strings().prowlarr_search_hint.localized())
        .confirmationDialog(
            MR.strings().grab_release_title.localized(),
            isPresented: Binding(
                get: { grabTarget != nil },
                set: { if !$0 { grabTarget = nil } }
            ),
            titleVisibility: .visible
        ) {
            if let result = grabTarget {
                Button(MR.strings().grab.localized()) {
                    grabbingGuid = result.guid
                    viewModel.grabRelease(result)
                    grabTarget = nil
                }
                Button(MR.strings().cancel.localized(), role: .cancel) {
                    grabTarget = nil
                }
            }
        } message: {
            if let result = grabTarget {
                Text("Send \"\(result.title ?? MR.strings().unknown.localized())\" to your download client?")
            }
        }
        .onChange(of: viewModel.grabStatus is OperationStatusSuccess) { _, isSuccess in
            if isSuccess {
                toastMessage = MR.strings().download_queue_success.localized()
                grabbingGuid = nil
                viewModel.resetGrabStatus()
            }
        }
        .onChange(of: viewModel.grabStatus is OperationStatusError) { _, isError in
            if isError {
                let error = viewModel.grabStatus as? OperationStatusError
                toastMessage = error?.message ?? MR.strings().error.localized()
                grabbingGuid = nil
                viewModel.resetGrabStatus()
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
    }
    
    @ViewBuilder
    private var searchContent: some View {
        if viewModel.searchResults is ProwlarrSearchStateInitial {
            initialView
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if viewModel.searchResults is ProwlarrSearchStateLoading {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if viewModel.searchResults is ProwlarrSearchStateError {
            if let error = viewModel.searchResults as? ProwlarrSearchStateError {
                errorView(message: error.message)
            }
        } else if viewModel.searchResults is ProwlarrSearchStateSuccess {
            if let success = viewModel.searchResults as? ProwlarrSearchStateSuccess {
                if success.items.isEmpty {
                    emptyView
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        VStack(spacing: 12) {
                            // Use \.offset as key — guid is nullable and can't be used directly
                            ForEach(Array(success.items.enumerated()), id: \.offset) { _, result in
                                SearchResultRow(
                                    result: result,
                                    isGrabbing: grabbingGuid != nil && grabbingGuid == result.guid,
                                    onGrab: { grabTarget = result }
                                )
                            }
                        }
                        .padding(.vertical, 12)
                        .padding(.horizontal, 16)
                    }
                }
            }
        } else {
            EmptyView()
        }
    }
    
    @ViewBuilder
    private var initialView: some View {
        VStack(spacing: 12) {
            Image(systemName: "magnifyingglass.circle")
                .font(.system(size: 64))
                .foregroundStyle(.secondary)
            Text(MR.strings().prowlarr_search_hint.localized())
                .font(.system(size: 17))
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
        }
    }
    
    @ViewBuilder
    private var emptyView: some View {
        VStack(spacing: 12) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 64))
                .foregroundStyle(.secondary)
            Text(MR.strings().no_results_found.localized())
                .font(.system(size: 17))
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

struct SearchResultRow: View {
    let result: ProwlarrSearchResult
    let isGrabbing: Bool
    let onGrab: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top) {
                Text(result.title ?? MR.strings().unknown.localized())
                    .font(.system(size: 15, weight: .semibold))
                    .lineLimit(2)
                
                Spacer()
                
                Button(action: onGrab) {
                    if isGrabbing {
                        ProgressView()
                            .frame(width: 22, height: 22)
                    } else {
                        Image(systemName: "arrow.down.circle")
                            .font(.system(size: 22))
                    }
                }
                .disabled(isGrabbing)
            }
            
            HStack(spacing: 8) {
                Text(result.indexer ?? MR.strings().unknown.localized())
                    .font(.caption)
                    .foregroundStyle(.secondary)
                
                Text("•")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                
                Text(protocolDisplayName(for: result.protocol))
                    .font(.caption)
                    .foregroundStyle(protocolColor(for: result.protocol))
                
                Text("•")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                
                Text(ByteCountFormatter.string(fromByteCount: result.size, countStyle: .binary))
                    .font(.caption)
                    .foregroundStyle(.secondary)
                
                Text("•")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                
                Text("\(result.age)d")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            
            if result.protocol == ReleaseProtocol.torrent {
                HStack(spacing: 12) {
                    HStack(spacing: 2) {
                        Image(systemName: "arrow.up")
                            .font(.caption)
                            .foregroundStyle(.green)
                        Text("\(result.seeders ?? 0)")
                            .font(.caption)
                            .foregroundStyle(.green)
                    }
                    
                    HStack(spacing: 2) {
                        Image(systemName: "arrow.down")
                            .font(.caption)
                            .foregroundStyle(.red)
                        Text("\(result.leechers ?? 0)")
                            .font(.caption)
                            .foregroundStyle(.red)
                    }
                }
            }
            
            if !result.categories.isEmpty {
                WrappingHStack(spacing: 4) {
                    ForEach(Array(result.categories.prefix(3).enumerated()), id: \.offset) { _, category in
                        Text(category.name ?? "Category \(category.id)")
                            .font(.caption2)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color(.systemGray5))
                            .cornerRadius(4)
                    }
                }
            }
        }
        .padding(12)
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }
    
    private func protocolDisplayName(for proto: ReleaseProtocol?) -> String {
        guard let proto = proto else { return MR.strings().unknown.localized() }
        switch proto {
        case ReleaseProtocol.torrent: return "Torrent"
        case ReleaseProtocol.usenet: return "Usenet"
        default: return MR.strings().unknown.localized()
        }
    }
    
    private func protocolColor(for proto: ReleaseProtocol?) -> Color {
        guard let proto = proto else { return .gray }
        switch proto {
        case ReleaseProtocol.torrent: return .blue
        case ReleaseProtocol.usenet: return .green
        default: return .gray
        }
    }
}

// Simple toast view
struct ToastView: View {
    let message: String
    
    var body: some View {
        Text(message)
            .font(.system(size: 14, weight: .medium))
            .foregroundColor(.white)
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(Color.black.opacity(0.75))
            .cornerRadius(20)
            .padding(.bottom, 24)
    }
}

// Wrapping horizontal stack for category chips
struct WrappingHStack: Layout {
    var spacing: CGFloat = 4
    
    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = FlowResult(in: proposal.width ?? 0, subviews: subviews, spacing: spacing)
        return result.size
    }
    
    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = FlowResult(in: bounds.width, subviews: subviews, spacing: spacing)
        for (index, subview) in subviews.enumerated() {
            subview.place(at: CGPoint(x: bounds.minX + result.positions[index].x,
                                      y: bounds.minY + result.positions[index].y),
                         proposal: .unspecified)
        }
    }
    
    struct FlowResult {
        var size: CGSize = .zero
        var positions: [CGPoint] = []
        
        init(in maxWidth: CGFloat, subviews: Subviews, spacing: CGFloat) {
            var x: CGFloat = 0
            var y: CGFloat = 0
            var rowHeight: CGFloat = 0
            
            for subview in subviews {
                let size = subview.sizeThatFits(.unspecified)
                if x + size.width > maxWidth, x > 0 {
                    x = 0
                    y += rowHeight + spacing
                    rowHeight = 0
                }
                positions.append(CGPoint(x: x, y: y))
                rowHeight = max(rowHeight, size.height)
                x += size.width + spacing
            }
            
            self.size = CGSize(width: maxWidth, height: y + rowHeight)
        }
    }
}
