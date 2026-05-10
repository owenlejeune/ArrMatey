//
//  DownloadQueueItemView.swift
//  iosApp
//

import SwiftUI
import Shared

struct DownloadQueueItemView: View {
    let item: DownloadItem
    let showClientInfo: Bool

    private var statusColor: Color {
        switch item.status {
        case .downloading, .downloadingForced, .downloadingMetadataForced, .checking, .checkingResumeData, .moving, .downloadingStalled:
            return .arrGreen
        case .uploading, .uploadingForced:
            return .arrBlue
        case .downloadingPaused, .uploadingPaused:
            return .arrPurple
        case .queued, .allocating, .propagating, .fetching:
            return .arrGrey
        case .error, .missingFiles, .unknown:
            return .arrRed
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(item.name.breakable())
                .font(.headline)
                .lineLimit(2)
                .truncationMode(.tail)

            statusView
            
            progressView
            
            ProgressView(value: Double(item.progress))
                .tint(statusColor)
                .background(statusColor.opacity(0.3))
                .padding(.vertical, 8)
            
            bottomRow
            
            if !item.category.isEmpty || !item.tags.isEmpty {
                tagsView
            }
        }
        .padding(16)
        .background(Color(uiColor: .secondarySystemBackground))
        .cornerRadius(12)
    }
    
    @ViewBuilder
    private var statusView: some View {
        HStack(spacing: 4) {
            Text(item.status.resource.localized())
                .foregroundColor(statusColor)
            
            if item.downloadSpeed > 0 {
                Text(" • ")
                Text("↓")
                Text(item.downloadSpeed.bytesAsFileSizeString() + "/s")
            }
            
            if item.uploadSpeed > 0 {
                Text(" • ")
                Text("↑")
                Text(item.uploadSpeed.bytesAsFileSizeString() + "/s")
            }
        }
        .font(.subheadline)
        .foregroundStyle(.secondary)
    }
    
    @ViewBuilder
    private var progressView: some View {
        HStack(spacing: 4) {
            Text(item.downloaded.bytesAsFileSizeString())
            Text(" / ")
            Text(item.size.bytesAsFileSizeString())
            Text(" • ")
            Text("\(Int(item.progress * 100))%")
        }
        .font(.subheadline)
        .foregroundStyle(.secondary)
    }
    
    @ViewBuilder
    private var bottomRow: some View {
        HStack(alignment: .center, spacing: 4) {
            if !item.etaString.trimmingCharacters(in: .whitespaces).isEmpty {
                Text("ETA: \(item.etaString)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            
            Spacer()
            
            if showClientInfo {
                HStack(spacing: 6) {
                    item.client.type.icon.toImage(renderingMode: .original)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 16, height: 16)
                    
                    Text(item.client.label)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
        }
        .padding(.top, 4)
    }
    
    @ViewBuilder
    private var tagsView: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                if !item.category.isEmpty {
                    AssistChip(label: item.category, color: .themePrimaryContainer, textColor: .themeOnPrimaryContainer)
                }
                
                ForEach(item.tags, id: \.self) { tag in
                    AssistChip(label: tag, color: .themeTertiaryContainer, textColor: .themeOnTertiaryContainer)
                }
            }
        }
    }
}

struct AssistChip: View {
    let label: String
    let color: Color
    let textColor: Color
    
    var body: some View {
        Text(label)
            .font(.caption)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(color)
            .foregroundColor(textColor)
            .clipShape(Capsule())
    }
}
