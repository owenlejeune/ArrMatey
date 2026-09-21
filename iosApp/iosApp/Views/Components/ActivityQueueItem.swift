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
    var useFullColorCards: Bool = false
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

    private var associatedColor: Color {
        item.type.associatedSwiftColor
    }

    private var backgroundColor: Color {
        if item.hasIssue {
            return Color.red.opacity(0.12)
        } else if useFullColorCards {
            return associatedColor
        } else {
            return Color(uiColor: .secondarySystemGroupedBackground)
        }
    }

    private var contentColor: Color {
        if item.hasIssue {
            return Color.red
        } else if useFullColorCards {
            return Color.black
        } else {
            return Color.primary
        }
    }

    private var secondaryContentColor: Color {
        if item.hasIssue {
            return Color.red.opacity(0.85)
        } else if useFullColorCards {
            return Color.black.opacity(0.75)
        } else {
            return Color.secondary
        }
    }

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 0) {
                if !useFullColorCards {
                    Rectangle()
                        .fill(item.hasIssue ? Color.red : associatedColor)
                        .frame(width: 5)
                }

                HStack(alignment: .center, spacing: 10) {
                    VStack(alignment: .leading, spacing: 3) {
                        Text(item.titleLabel)
                            .font(.body.weight(.medium))
                            .foregroundStyle(contentColor)
                            .lineLimit(2)
                        
                        Text(statusText)
                            .font(.subheadline)
                            .foregroundStyle(secondaryContentColor)
                        
                        if let instanceName = item.instanceName, !instanceName.isEmpty {
                            Text(instanceName)
                                .font(.caption2.weight(.medium))
                                .foregroundStyle(useFullColorCards ? Color.black.opacity(0.6) : Color.secondary)
                        }
                    }
                    
                    Spacer()
                    
                    if item.hasIssue {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundStyle(contentColor)
                            .font(.title3)
                    }
                }
                .padding(.vertical, 12)
                .padding(.horizontal, 16)
            }
            .background(backgroundColor)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(item.hasIssue ? Color.red.opacity(0.3) : (useFullColorCards ? Color.clear : Color.primary.opacity(0.06)), lineWidth: 0.5)
            )
        }
        .buttonStyle(.plain)
    }
}

