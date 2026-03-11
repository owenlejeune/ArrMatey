//
//  DownloadQueueItemView.swift
//  iosApp
//

import SwiftUI
import Shared

struct DownloadQueueItemView: View {
    let item: DownloadItem
    let actionInProgress: Bool
    let onPause: () -> Void
    let onResume: () -> Void
    let onDelete: () -> Void
    let showClientInfo: Bool

    private var canPause: Bool { item.status == .downloading || item.status == .seeding }
    private var canResume: Bool { item.status == .paused || item.status == .stalled }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(item.name)
                        .font(.body)
                        .fontWeight(.medium)
                        .lineLimit(2)
                    Text(item.status.resource.localized())
                        
                }
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Spacer()
                HStack(spacing: 4) {
                    if canPause {
                        Button(action: onPause) {
                            Image(systemName: "pause.fill")
                        }
                        .disabled(actionInProgress)
                    } else if canResume {
                        Button(action: onResume) {
                            Image(systemName: "play.fill")
                        }
                        .disabled(actionInProgress)
                    }
                    Button(role: .destructive, action: onDelete) {
                        Image(systemName: "trash")
                    }
                    .disabled(actionInProgress)
                }
                .buttonStyle(.bordered)
                .controlSize(.small)
            }

            ProgressView(value: item.progress)
                .tint(progressColor)

            HStack(spacing: 6) {
                if !item.etaString.isEmpty {
                    Text("ETA: " + item.etaString)
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                }
                
                Spacer()
                
                if showClientInfo {
                    HStack(spacing: 8) {
                        item.client.type.icon.toImage(renderingMode: .original)
                        
                        Text(item.client.label)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }
        }
        .padding(12)
        .background(Color(uiColor: .secondarySystemBackground))
        .cornerRadius(12)
    }

    private var progressColor: Color {
        switch item.status {
        case .failed:  return .red
        case .stalled: return .orange
        case .seeding: return .green
        default:       return .accentColor
        }
    }
}


