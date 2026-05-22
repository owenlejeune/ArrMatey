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
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 12) {
                Text(item.eventType.resource.localized())
                    .font(.system(size: 14))
                    .foregroundColor(.themePrimary)
                Text(item.date.format(pattern: "MMM d, yyyy"))
                    .font(.system(size: 12))
            }
            Text(item.displayTitle?.breakable() ?? "---")
                .fontWeight(.semibold)
            
            Text(subLabel)
                .font(.system(size: 12))
                .foregroundColor(.secondary)
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 10, style: .continuous)
                .fill(Color(.systemGroupedBackground))
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
