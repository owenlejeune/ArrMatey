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
            return Color.red.opacity(0.15)
        } else {
            return Color(uiColor: .secondarySystemBackground)
        }
    }

    private var contentColor: Color {
        if item.hasIssue {
            return Color.red.opacity(0.9)
        } else {
            return Color.primary
        }
    }

    var body: some View {
        Button(action: onClick) {
            HStack(alignment: .center, spacing: 4) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(item.titleLabel)
                        .font(.body)
                        .fontWeight(.medium)
                        .foregroundColor(contentColor)
                    
                    Text(statusText)
                        .font(.system(size: 14))
                        .foregroundColor(contentColor.opacity(0.8))
                    
                    Text(item.instanceName ?? "")
                        .font(.system(size: 12))
                }
                
                Spacer()
                
                if (item.hasIssue) {
                    Image(systemName: "exclamationmark.triangle")
                        .imageScale(.medium)
                        .foregroundColor(.red)
                }
            }
            .padding(.vertical, 12)
            .padding(.horizontal, 16)
            .background(backgroundColor)
            .cornerRadius(12)
        }
        .buttonStyle(.plain)
    }
    
}
