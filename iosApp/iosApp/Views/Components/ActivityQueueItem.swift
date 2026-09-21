//
//  ActivityQueueItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-21.
//

import SwiftUI
import Shared

struct ActivityQueueItem: View {
    let item: QueueItem
    let onClick: () -> Void
    
    private var statusText: String {
        var text = item.statusLabel
        
        if item.trackedDownloadState == .downloading {
            text += " • \(item.progressLabel)"
            
            if let remainingTime = item.remainingTimeLabel {
                text += " • \(remainingTime)"
            }
        }
        
        return text
    }

    private var backgroundColor: Color {
        if item.hasIssue {
            return Color.red.opacity(0.12)
        } else {
            return Color(uiColor: .secondarySystemGroupedBackground)
        }
    }

    private var contentColor: Color {
        if item.hasIssue {
            return Color.red
        } else {
            return Color.primary
        }
    }

    var body: some View {
        Button(action: onClick) {
            HStack(alignment: .center, spacing: 10) {
                VStack(alignment: .leading, spacing: 3) {
                    Text(item.titleLabel)
                        .font(.body.weight(.medium))
                        .foregroundStyle(contentColor)
                        .lineLimit(2)
                    
                    Text(statusText)
                        .font(.subheadline)
                        .foregroundStyle(item.hasIssue ? Color.red.opacity(0.85) : Color.secondary)
                    
                    if let instanceName = item.instanceName, !instanceName.isEmpty {
                        Text(instanceName)
                            .font(.caption2.weight(.medium))
                            .foregroundStyle(.tertiary)
                    }
                }
                
                Spacer()
                
                if (item.hasIssue) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .foregroundStyle(.red)
                        .font(.title3)
                }
            }
            .padding(.vertical, 12)
            .padding(.horizontal, 16)
            .background(backgroundColor)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(item.hasIssue ? Color.red.opacity(0.3) : Color.primary.opacity(0.06), lineWidth: 0.5)
            )
        }
        .buttonStyle(.plain)
    }
}

