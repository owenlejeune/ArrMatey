//
//  HistoryItemView.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-23.
//

import Shared
import SwiftUI

struct HistoryItemView: View {
    let item: HistoryItem
    
    private var associatedColor: Color {
        item.instanceType?.associatedSwiftColor ?? .accentColor
    }
    
    var body: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(associatedColor)
                .frame(width: 5)
            
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 12) {
                    Text(item.eventType.resource.localized())
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundColor(.themePrimary)
                    Text(item.date.format(pattern: "MMM d, yyyy"))
                        .font(.system(size: 12))
                        .foregroundColor(.secondary)
                    Spacer()
                    if let instanceName = item.instanceName, !instanceName.isEmpty {
                        Text(instanceName)
                            .font(.system(size: 11, weight: .medium))
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(4)
                            .foregroundColor(.secondary)
                    }
                }
                Text(item.displayTitle?.breakable() ?? "---")
                    .fontWeight(.semibold)
                
                Text(subLabel)
                    .font(.system(size: 12))
                    .foregroundColor(.secondary)
            }
            .padding(.vertical, 12)
            .padding(.horizontal, 16)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .background(Color(uiColor: .secondarySystemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .stroke(Color.primary.opacity(0.06), lineWidth: 0.5)
        )
    }
    
    private var subLabel: String {
        var components: [String] = []
        
        if let qualityLabel = item.quality?.qualityLabel {
            components.append(qualityLabel)
        }
        components.append(LabelUtilsKt.singleLanguageLabel(item.languages))
        
        if let indexer = item.indexerLabel {
            components.append(indexer)
        }
        
        return components.joined(separator: " • ")
    }
}
